package phanastrae.ywsanf.structure;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.block.YWSaNFBlocks;
import phanastrae.ywsanf.mixin.StructureStartAccessor;
import phanastrae.ywsanf.util.IntNoise2D;

import java.util.List;
import java.util.Optional;

public class StructurePlacer {
    public static final IntNoise2D NOISE = generateNoise();
    public static final int MAX_NOISE_DELAY = 5;
    public static final int CONVERSION_DELAY = 50;
    public static final int SPAWN_TIME_PADDING = 8;

    private Stage stage = Stage.ERROR;

    private final ResourceLocation structureRL;
    private final BlockPos pos;

    @Nullable
    private Structure structure;
    @Nullable
    private StructureStart structureStart;
    @Nullable
    private IntermediateStructureStorage intermediateStructureStorage;
    @Nullable
    private BlockPos structureOrigin;
    private List<StructurePiece> pieces = new ObjectArrayList<>();

    private int currentSpawnTime = 0;
    private int minSpawnTime = 0;

    public StructurePlacer(ResourceLocation structureRL, BlockPos pos) {
        this.structureRL = structureRL;
        this.stage = Stage.HAS_RESOURCE_LOCATION;
        this.pos = pos;
    }

    public enum Stage {
        ERROR("error", -1),
        HAS_RESOURCE_LOCATION("has_resource_location", 20),
        HAS_STRUCTURE("has_structure", 20),
        FILLING_STORAGE("filling_storage", 1),
        PLACED_PIECES("placed_pieces", 30),
        FILLED_STORAGE("filled_storage", 10),
        READY_TO_PLACE("ready_to_place",1),
        PLACED_STABLES("placed_stables", 10),
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
            Optional<Structure> structureOptional = getStructure(serverLevel.registryAccess(), this.structureRL);
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
        } else if(this.stage == Stage.FILLED_STORAGE && this.intermediateStructureStorage != null && this.structureStart != null) {
            // move fragile blocks (ie block entities, doors, torches, etc.) to a separate storage
            this.intermediateStructureStorage.forEachContainer(((sectionPos, boxedContainer) -> {
                BoundingBox box = boxedContainer.getBox();
                if(box != null) {
                    BoxedContainer fragilesContainer = new BoxedContainer();
                    BlockState feastingTar = YWSaNFBlocks.FEASTING_TAR.defaultBlockState();

                    for(int x = box.minX(); x <= box.maxX(); x++) {
                        for(int y = box.minY(); y <= box.maxY(); y++) {
                            for(int z = box.minZ(); z <= box.maxZ(); z++) {
                                BlockState state = boxedContainer.get(x, y, z);
                                if(state.is(Blocks.STRUCTURE_VOID)) {
                                    continue;
                                }
                                if(isStateFragile(state)) {
                                    boxedContainer.set(x, y, z, feastingTar);
                                    fragilesContainer.set(x, y, z, state);
                                }
                            }
                        }
                    }

                    if(fragilesContainer.getBox() != null) {
                        this.intermediateStructureStorage.addFragileContainer(sectionPos, fragilesContainer);
                    }
                }
            }));

            BoundingBox box = this.structureStart.getBoundingBox();
            int[] times = calcSpawnTimes(box, this.pos);
            this.minSpawnTime = times[0];
            this.currentSpawnTime = times[1];

            this.stage = Stage.READY_TO_PLACE;
            return true;
        } else if(this.stage == Stage.READY_TO_PLACE && this.intermediateStructureStorage != null && this.structureStart != null) {
            // place stable structure storage
            placeStoredStructureStables(this.intermediateStructureStorage, this.currentSpawnTime, this.pos, serverLevel);

            if(this.currentSpawnTime < this.minSpawnTime) {
                this.stage = Stage.PLACED_STABLES;
            }
            this.currentSpawnTime--;

            return true;
        } else if(this.stage == Stage.PLACED_STABLES && this.intermediateStructureStorage != null) {
            // place fragile structure storage, block entities, entities
            placeStoredStructureSpecials(this.intermediateStructureStorage, serverLevel);

            this.stage = Stage.COMPLETED;
            return true;
        } else {
            return this.stage == Stage.COMPLETED;
        }
    }

    public boolean isStateFragile(BlockState state) {
        if(state.getBlock() instanceof EntityBlock) {
            return true;
        } else {
            return false; // TODO
        }
    }

    public static boolean isStateFeastable(BlockState state, BlockGetter level, BlockPos pos) {
        Block block = state.getBlock();
        if(block instanceof EntityBlock) {
            // cannot consume block entities
            return false;
        }

        float destroySpeed = state.getDestroySpeed(level, pos);
        if(destroySpeed == -1.0F) {
            // cannot feast upon indestructible blocks, ie bedrock
            return false;
        }

        return true;
    }

    public static boolean isStateSubsumed(BlockState state) {
        return state.is(YWSaNFBlocks.FEASTING_TAR);
    }

    public static Optional<Structure> getStructure(RegistryAccess registryAccess, ResourceLocation resourceLocation) {
        ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, resourceLocation);

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

    public static void placeStoredStructureStables(IntermediateStructureStorage intermediateStorage, int currentSpawnTime, BlockPos pos, WorldGenLevel level) {
        // TODO update neighbors?
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockState feastingTAR = YWSaNFBlocks.FEASTING_TAR.defaultBlockState();
        intermediateStorage.forEachContainer((sectionPos, boxedContainer) -> {
            BoundingBox box = boxedContainer.getBox();
            if(box == null) return;

            int mx = sectionPos.minBlockX();
            int my = sectionPos.minBlockY();
            int mz = sectionPos.minBlockZ();
            if (!isBoxInTimeRange(box, pos.subtract(new Vec3i(mx, my, mz)), currentSpawnTime)) return;

            for (int x = box.minX(); x <= box.maxX(); x++) {
                int dx = mx + x - pos.getX();
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    int dz = mz + z - pos.getZ();

                    double r = calcHorizontalFactor(dx, dz);
                    double noise = NOISE.get(dx, dz);

                    for (int y = box.minY(); y <= box.maxY(); y++) {
                        int dy = my + y - pos.getY();

                        double t = calcSpawnTime(r, dy, noise);
                        if(tInRange(t, currentSpawnTime)) {
                            BlockState newState = boxedContainer.get(x, y, z);
                            if (!newState.is(Blocks.STRUCTURE_VOID)) {
                                mutableBlockPos.set(mx + x, my + y, mz + z);

                                BlockState oldState = level.getBlockState(mutableBlockPos);
                                if(isStateSubsumed(oldState)) {
                                    level.setBlock(mutableBlockPos, newState, 2, 0);
                                }
                            }
                        } else if(tInRange(t + CONVERSION_DELAY, currentSpawnTime)) {
                            BlockState newState = boxedContainer.get(x, y, z);
                            if (!newState.is(Blocks.STRUCTURE_VOID)) {
                                mutableBlockPos.set(mx + x, my + y, mz + z);

                                BlockState oldState = level.getBlockState(mutableBlockPos);
                                if(newState.isAir() && oldState.isAir()) {
                                    continue;
                                }
                                if(isStateFeastable(oldState, level, mutableBlockPos)) {
                                    level.setBlock(mutableBlockPos, feastingTAR, 2, 0);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public static void placeStoredStructureSpecials(IntermediateStructureStorage intermediateStorage, WorldGenLevel level) {
        // TODO prevent theoretical chest merging, tweak fragility criteria, update neigbors?
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        intermediateStorage.forEachFragileContainer((sectionPos, boxedContainer) -> {
            BoundingBox box = boxedContainer.getBox();
            if(box != null) {
                int mx = sectionPos.minBlockX();
                int my = sectionPos.minBlockY();
                int mz = sectionPos.minBlockZ();
                for (int x = box.minX(); x <= box.maxX(); x++) {
                    for (int y = box.minY(); y <= box.maxY(); y++) {
                        for (int z = box.minZ(); z <= box.maxZ(); z++) {
                            BlockState state = boxedContainer.get(x, y, z);
                            if (!state.is(Blocks.STRUCTURE_VOID)) {
                                mutableBlockPos.set(mx + x, my + y, mz + z);

                                BlockState oldState = level.getBlockState(mutableBlockPos);
                                if(isStateSubsumed(oldState)) {
                                    level.setBlock(mutableBlockPos, state, 2, 0);
                                }
                            }
                        }
                    }
                }
            }
        });
        intermediateStorage.forEachBlockEntity(((blockPos, blockEntity) -> {
            BlockState state = intermediateStorage.getFragileBlockState(blockPos);
            BlockState entityState = blockEntity.getBlockState();
            if(state.getBlock().equals(entityState.getBlock())) {
                level.getLevel().getChunkAt(blockPos).addAndRegisterBlockEntity(blockEntity);
            }
        }));
        intermediateStorage.forEachEntity(level::addFreshEntity);
    }

    private static boolean checkLoaded(ServerLevel level, ChunkPos start, ChunkPos end) {
        if (ChunkPos.rangeClosed(start, end).anyMatch(chunkPos -> !level.isLoaded(chunkPos.getWorldPosition()))) {
            return false;
        }

        return true;
    }

    public static boolean tInRange(double t, double time) {
        return time - 1 < t && t <= time;
    }

    public static double calcHorizontalFactor(double dx, double dz) {
        return Math.sqrt(dx*dx+dz*dz);
    }

    public static boolean isBoxInTimeRange(BoundingBox box, BlockPos pos, int currentSpawnTime) {
        int[] times = calcSpawnTimes(box, pos);
        int min = times[0];
        int max = times[1];

        if(currentSpawnTime - 1 >= max || min > currentSpawnTime) {
            return false;
        } else {
            return true;
        }
    }

    public static int[] calcSpawnTimes(BoundingBox box, BlockPos core) {
        // returns an int array of min, max
        int dMinX = box.minX() - core.getX();
        int dMaxX = box.maxX() - core.getX();
        int dMinZ = box.minZ() - core.getZ();
        int dMaxZ = box.maxZ() - core.getZ();

        // horizontal distance is maximised at one of the corners, so just check those
        double maxH = 0;
        for(int i = 0; i < 4; i++) {
            int x = (i & 0x1) == 0 ? dMinX : dMaxX;
            int z = (i & 0x2) == 0 ? dMinZ : dMaxZ;

            double h = calcHorizontalFactor(x, z);
            if(h > maxH) {
                maxH = h;
            }
        }

        int minDy = box.minY() - core.getY();
        int maxDy = box.maxY() - core.getY();

        // spawn times are linear in r, -dy, n so just input the appropriate min/max values of each parameter to min/maximise the time
        // time is min at closest (h=0), highest (dy=maxDy) point
        double minTime = calcSpawnTime(0, maxDy, 0);
        int min = Mth.floor(minTime) - SPAWN_TIME_PADDING;
        // time is low at furthest (h=maxH), lowest (dy=minDy) point
        double maxTime = calcSpawnTime(maxH, minDy, MAX_NOISE_DELAY - 1);
        int max = Mth.ceil(maxTime) + CONVERSION_DELAY + SPAWN_TIME_PADDING;

        return new int[]{min, max};
    }

    public static double calcSpawnTime(double h, double dy, double n) {
        return h * 3.5F + 0.6F * -dy + n * 2.4F;
    }

    public static IntNoise2D generateNoise() {
        RandomSource randomSource = RandomSource.create(12345);
        return IntNoise2D.generateNoise(128, 128, () -> randomSource.nextInt(MAX_NOISE_DELAY));
    }
}
