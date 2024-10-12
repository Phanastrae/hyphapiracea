package phanastrae.ywsanf.structure;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.mixin.StructureStartAccessor;

import java.util.List;
import java.util.Optional;

public class StructurePlacer {

    private Stage stage = Stage.ERROR;

    private final ResourceLocation structureRL;
    private final BlockPos pos;

    @Nullable
    private Structure structure;
    @Nullable
    private StructureStart structureStart;
    @Nullable
    private IntermediateStructureStorage intermediateStructureStorage;
    private List<StructurePiece> pieces = new ObjectArrayList<>();
    @Nullable
    private BlockPos structureOrigin;

    public StructurePlacer(ResourceLocation structureRL, BlockPos pos) {
        this.structureRL = structureRL;
        this.stage = Stage.HAS_RESOURCE_LOCATION;
        this.pos = pos;
    }

    public enum Stage {
        ERROR("error", -1),
        HAS_RESOURCE_LOCATION("has_resource_location", 20),
        HAS_STRUCTURE("has_structure", 20),
        FILLING_STORAGE("filling_storage", 3),
        PLACED_PIECES("placed_pieces", 20),
        FILLED_STORAGE("filled_storage", 20),
        COMPLETED("completed", -1);

        private final String id;
        private final int wait;

        Stage(String id, int wait) {
            this.id = id;
            this.wait = wait;
        }

        public String getId() {
            return this.id;
        }

        public int getWait() {
            return this.wait;
        }
    }

    public Stage getStage() {
        return this.stage;
    }

    public boolean advance(ServerLevel serverLevel) {
        if(this.stage == Stage.HAS_RESOURCE_LOCATION) {
            // get structure from RL
            Optional<Structure> structureOptional = getStructure(serverLevel.registryAccess());
            if(structureOptional.isEmpty()) {
                this.stage = Stage.ERROR;
                return false;
            }

            this.structure = structureOptional.get();

            this.stage = Stage.HAS_STRUCTURE;
            return true;
        } else if(this.stage == Stage.HAS_STRUCTURE && this.structure != null) {
            // get structure start from structure
            StructureStart structureStart = getStructureStart(this.structure, this.pos, serverLevel);
            if (!structureStart.isValid()) {
                this.stage = Stage.ERROR;
                return false;
            }

            this.structureStart = structureStart;
            this.pieces.addAll(structureStart.getPieces());
            if(!this.pieces.isEmpty()) {
                BoundingBox boundingBox = this.pieces.getFirst().getBoundingBox();
                BlockPos boxCenter = boundingBox.getCenter();
                this.structureOrigin = new BlockPos(boxCenter.getX(), boundingBox.minY(), boxCenter.getZ());
            }
            this.intermediateStructureStorage = new IntermediateStructureStorage();

            this.stage = Stage.FILLING_STORAGE;
            return true;
        } else if(this.stage == Stage.FILLING_STORAGE && this.structureStart != null && this.intermediateStructureStorage != null) {
            // fill structure storage from structureStart, adding the pieces one by one until done
            if(!this.pieces.isEmpty()) {
                if(this.structureOrigin == null) {
                    this.stage = Stage.ERROR;
                    return false;
                }

                if(!placeStructurePiece(
                        this.pieces.getFirst(),
                        new IntermediateGenLevel(this.intermediateStructureStorage, serverLevel),
                        serverLevel,
                        serverLevel.structureManager(),
                        serverLevel.getChunkSource().getGenerator(),
                        serverLevel.getRandom(),
                        this.structureOrigin)) {
                    this.stage = Stage.ERROR;
                    return false;
                } else {
                    this.pieces.removeFirst();
                }
            }

            if(this.pieces.isEmpty()) {
                this.stage = Stage.PLACED_PIECES;
            }
            return true;
        } else if(this.stage == Stage.PLACED_PIECES && this.structureStart != null && this.intermediateStructureStorage != null) {
            // fill structure storage from structureStart, doing the after place stuff
            if(!fillAfterPlace(this.intermediateStructureStorage, this.structureStart, serverLevel)) {
                this.stage = Stage.ERROR;
                return false;
            }

            this.stage = Stage.FILLED_STORAGE;
            return true;
        } else if(this.stage == Stage.FILLED_STORAGE && this.intermediateStructureStorage != null) {
            // place structure storage
            placeStoredStructure(this.intermediateStructureStorage, serverLevel);

            this.stage = Stage.COMPLETED;
            return true;
        } else {
            return this.stage == Stage.COMPLETED;
        }
    }

    public Optional<Structure> getStructure(RegistryAccess registryAccess) {
        ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, this.structureRL);

        Optional<Registry<Structure>> structureRegistryOptional = registryAccess.registry(Registries.STRUCTURE);
        if (structureRegistryOptional.isEmpty()) {
            return Optional.empty();
        }

        Optional<Holder.Reference<Structure>> structureReferenceOptional = structureRegistryOptional.get().getHolder(key);
        return structureReferenceOptional.map(Holder.Reference::value);

    }

    public static StructureStart getStructureStart(Structure structure, BlockPos pos, ServerLevel serverLevel) {
        ChunkGenerator chunkgenerator = serverLevel.getChunkSource().getGenerator();
        return structure.generate(
                serverLevel.registryAccess(),
                chunkgenerator,
                chunkgenerator.getBiomeSource(),
                serverLevel.getChunkSource().randomState(),
                serverLevel.getStructureManager(),
                serverLevel.getSeed(),
                new ChunkPos(pos),
                0,
                serverLevel,
                biome -> true
        );
    }

    public static boolean placeStructurePiece(StructurePiece structurePiece, WorldGenLevel worldGenLevel , ServerLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BlockPos structureOrigin) {
        BoundingBox boundingBox = structurePiece.getBoundingBox();
        ChunkPos startPos = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.minX()), SectionPos.blockToSectionCoord(boundingBox.minZ()));
        ChunkPos endPos = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.maxX()), SectionPos.blockToSectionCoord(boundingBox.maxZ()));
        if(!checkLoaded(level, startPos, endPos)) {
            return false;
        }

        try {
            ChunkPos.rangeClosed(startPos, endPos)
                    .forEach(
                            chunkPos -> {
                                BoundingBox chunkBox =
                                        new BoundingBox(
                                                chunkPos.getMinBlockX(),
                                                level.getMinBuildHeight(),
                                                chunkPos.getMinBlockZ(),
                                                chunkPos.getMaxBlockX(),
                                                level.getMaxBuildHeight(),
                                                chunkPos.getMaxBlockZ()
                                        );
                                structurePiece.postProcess(worldGenLevel, structureManager, generator, random, chunkBox, chunkPos, structureOrigin);
                            }
                    );
        } catch (Exception e) {
            YWSaNF.LOGGER.error("Error trying to generate structure!"); // TODO consider changing this
            return false;
        }

        return true;
    }

    public static boolean fillAfterPlace(IntermediateStructureStorage intermediateStructureStorage, StructureStart structureStart, ServerLevel serverLevel) {
        BoundingBox boundingBox = structureStart.getBoundingBox();
        ChunkPos startPos = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.minX()), SectionPos.blockToSectionCoord(boundingBox.minZ()));
        ChunkPos endPos = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.maxX()), SectionPos.blockToSectionCoord(boundingBox.maxZ()));
        if(!checkLoaded(serverLevel, startPos, endPos)) {
            return false;
        }

        IntermediateGenLevel intermediateGenLevel = new IntermediateGenLevel(intermediateStructureStorage, serverLevel);
        int minHeight = serverLevel.getMinBuildHeight();
        int maxHeight = serverLevel.getMaxBuildHeight();
        StructureManager structureManager = serverLevel.structureManager();
        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        RandomSource randomSource = serverLevel.getRandom();

        try {
            ChunkPos.rangeClosed(startPos, endPos)
                    .forEach(
                            chunkPos -> afterPlaceInChunk(
                                    structureStart,
                                    intermediateGenLevel,
                                    structureManager,
                                    chunkGenerator,
                                    randomSource,
                                    new BoundingBox(
                                            chunkPos.getMinBlockX(),
                                            minHeight,
                                            chunkPos.getMinBlockZ(),
                                            chunkPos.getMaxBlockX(),
                                            maxHeight,
                                            chunkPos.getMaxBlockZ()
                                    ),
                                    chunkPos)
                    );
        } catch (Exception e) {
            YWSaNF.LOGGER.error("Error trying to generate structure!"); // TODO consider changing this
            return false;
        }

        return true;
    }

    public static void afterPlaceInChunk(StructureStart structureStart, WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos) {
        PiecesContainer piecesContainer = ((StructureStartAccessor)(Object)structureStart).getPieceContainer();
        List<StructurePiece> list = piecesContainer.pieces();
        if (!list.isEmpty()) {
            structureStart.getStructure().afterPlace(level, structureManager, generator, random, chunkBox, chunkPos, piecesContainer);
        }
    }

    public static void placeStoredStructure(IntermediateStructureStorage intermediateStorage, WorldGenLevel level) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        intermediateStorage.forEachContainer((sectionPos, blockStatePalettedContainer) -> {
            int mx = sectionPos.minBlockX();
            int my = sectionPos.minBlockY();
            int mz = sectionPos.minBlockZ();
            for(int x = 0; x < 16; x++) {
                for(int y = 0; y < 16; y++) {
                    for(int z = 0; z < 16; z++) {
                        BlockState state = blockStatePalettedContainer.get(x, y, z);
                        if (!state.is(Blocks.STRUCTURE_VOID)) {
                            mutableBlockPos.set(mx+x, my+y, mz+z);
                            level.setBlock(mutableBlockPos, state, 2, 0);
                        }
                    }
                }
            }
        });
        intermediateStorage.forEachBlockEntity(((blockPos, blockEntity) -> {
            level.getChunk(blockPos).setBlockEntity(blockEntity);
        }));
        intermediateStorage.forEachEntity(level::addFreshEntity);
    }

    private static boolean checkLoaded(ServerLevel level, ChunkPos start, ChunkPos end) {
        if (ChunkPos.rangeClosed(start, end).anyMatch(chunkPos -> !level.isLoaded(chunkPos.getWorldPosition()))) {
            return false;
        }

        return true;
    }
}
