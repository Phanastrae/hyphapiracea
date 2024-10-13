package phanastrae.ywsanf.structure;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.material.FluidState;
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

    private Stage stage = Stage.IDLE;
    private int maxProgress = 0;
    private int progress = 0;
    private int stepProgress = 0;

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
        this.pos = pos;
        this.setStage(Stage.GET_STRUCTURE, 1);
    }

    public enum Stage {
        ERROR("error", -1, false),
        IDLE("idle", -1, false),
        GET_STRUCTURE("get_structure", 20, true),
        GET_STRUCTURE_START("get_structure_start", 20, true),
        FILL_STORAGE_PIECES("fill_storage_pieces", 1, true),
        FILL_STORAGE_AFTER("fill_storage_after", 30, true),
        POST_PROCESS("post_process", 30, true),
        PLACE_BLOCKS("place_blocks",1, true),
        PLACE_SPECIALS("place_specials", 20, true),
        COMPLETED("completed", 60, false);

        private final String id;
        private final int wait;
        private final boolean showProgress;

        Stage(String id, int wait, boolean showProgress) {
            this.id = id;
            this.wait = wait;
            this.showProgress = showProgress;
        }

        public String getId() {
            return this.id;
        }

        public int getWait() {
            return this.wait;
        }

        public boolean shouldShowProgress() {
            return this.showProgress;
        }
    }

    public void setStage(Stage stage, int requiredOperations) {
        this.stage = stage;
        this.maxProgress = stage.getWait() * requiredOperations;
        this.progress = 0;
    }

    public void setStage(Stage stage) {
        this.setStage(stage, 1);
    }

    public Stage getStage() {
        return this.stage;
    }

    public int getProgressPercent() {
        if(this.maxProgress <= 0) {
            return 100;
        } else {
            float progressFraction = this.progress / (float)this.maxProgress;
            return Math.round(progressFraction * 100);
        }
    }

    // returns true if activity happened
    public boolean tick(ServerLevel serverLevel) {
        int wait = this.stage.wait;
        if(wait == -1) return false;

        this.progress++;
        this.stepProgress++;
        if(this.stepProgress >= wait) {
            this.stepProgress = 0;
            this.advance(serverLevel);
            return true;
        }
        return false;
    }

    public boolean advance(ServerLevel serverLevel) {
        if(this.stage == Stage.GET_STRUCTURE) {
            // get structure from RL
            Optional<Structure> structureOptional = getStructure(serverLevel.registryAccess(), this.structureRL);
            if(structureOptional.isEmpty()) {
                this.setStage(Stage.ERROR);
                return false;
            }

            this.structure = structureOptional.get();

            this.setStage(Stage.GET_STRUCTURE_START);
            return true;
        } else if(this.stage == Stage.GET_STRUCTURE_START && this.structure != null) {
            // TODO this may be able to be split further across time for jigsaw structures
            // get structure start from structure
            StructureStart structureStart = getStructureStart(this.structure, this.pos, serverLevel);
            if (!structureStart.isValid()) {
                this.setStage(Stage.ERROR);
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

            this.setStage(Stage.FILL_STORAGE_PIECES, this.pieces.size());
            return true;
        } else if(this.stage == Stage.FILL_STORAGE_PIECES && this.structureStart != null && this.intermediateStructureStorage != null) {
            // TODO this may be able to be split further across time for jigsaw structure pieces using ListPoolElement
            // fill structure storage from structureStart, adding the pieces one by one until done
            if(!this.pieces.isEmpty()) {
                if(this.structureOrigin == null) {
                    this.setStage(Stage.ERROR);
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
                    this.setStage(Stage.ERROR);
                    return false;
                } else {
                    this.pieces.removeFirst();
                }
            }

            if(this.pieces.isEmpty()) {
                this.setStage(Stage.FILL_STORAGE_AFTER);
            }
            return true;
        } else if(this.stage == Stage.FILL_STORAGE_AFTER && this.structureStart != null && this.intermediateStructureStorage != null) {
            // fill structure storage from structureStart, doing the after place stuff
            if(!fillAfterPlace(this.intermediateStructureStorage, this.structureStart, serverLevel, this.pos)) {
                this.setStage(Stage.ERROR);
                return false;
            }

            this.setStage(Stage.POST_PROCESS);
            return true;
        } else if(this.stage == Stage.POST_PROCESS && this.intermediateStructureStorage != null && this.structureStart != null) {
            // move fragile blocks (ie block entities, doors, torches, etc.) to a separate storage
            this.intermediateStructureStorage.forEachContainer(((sectionPos, boxedContainer) -> {
                BoundingBox box = boxedContainer.getBox();
                if(box != null) {
                    BoxedContainer fragilesContainer = new BoxedContainer();
                    BlockState feastingTar = YWSaNFBlocks.FEASTING_TAR.defaultBlockState();

                    BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                    int mx = sectionPos.minBlockX();
                    int my = sectionPos.minBlockY();
                    int mz = sectionPos.minBlockZ();
                    for(int x = box.minX(); x <= box.maxX(); x++) {
                        for(int y = box.minY(); y <= box.maxY(); y++) {
                            for(int z = box.minZ(); z <= box.maxZ(); z++) {
                                BlockState state = boxedContainer.get(x, y, z);
                                if(state.is(Blocks.STRUCTURE_VOID)) {
                                    continue;
                                }
                                mutableBlockPos.set(mx+x, my+y, mz+z);
                                if(isStateFragile(state, serverLevel, mutableBlockPos)) {
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

            this.setStage(Stage.PLACE_BLOCKS, this.currentSpawnTime - this.minSpawnTime + 2);
            return true;
        } else if(this.stage == Stage.PLACE_BLOCKS && this.intermediateStructureStorage != null && this.structureStart != null) {
            // place stable structure storage
            placeStoredStructureStables(this.intermediateStructureStorage, this.currentSpawnTime, this.pos, serverLevel);

            if(this.currentSpawnTime < this.minSpawnTime) {
                this.setStage(Stage.PLACE_SPECIALS);
                return true;
            }
            this.currentSpawnTime--;

            return true;
        } else if(this.stage == Stage.PLACE_SPECIALS && this.intermediateStructureStorage != null) {
            // place fragile structure storage, block entities, entities
            placeStoredStructureSpecials(this.intermediateStructureStorage, serverLevel);

            this.setStage(Stage.COMPLETED);
            return true;
        } else {
            return this.stage == Stage.COMPLETED;
        }
    }

    public boolean isStateFragile(BlockState state, LevelReader levelReader, BlockPos blockPos) {
        Block block = state.getBlock();
        if(block instanceof EntityBlock) {
            return true;
        }

        if(block instanceof DoorBlock) {
            // bottoms of doors are not always properly detected by the canSurvive check
            return true;
        }

        if(!state.canSurvive(levelReader, blockPos)) {
            // this prevents some, but not all, problems
            return true;
        }

        // TODO consider adding any other 'fragile' blocks here, if they exist (all redstone components? falling blocks? fluids?)

        return false;
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
        // TODO consider blacklisting other blocks? eg obsidian?, iron block?

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

    public static boolean placeStructurePiece(StructurePiece structurePiece, WorldGenLevel worldGenLevel, ServerLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BlockPos structureOrigin) {
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
            YWSaNF.LOGGER.error("Error trying to place piece for structure with Leukbox at {}!", structureOrigin.toString());
            return false;
        }

        return true;
    }

    public static boolean fillAfterPlace(IntermediateStructureStorage intermediateStructureStorage, StructureStart structureStart, ServerLevel serverLevel, BlockPos structureOrigin) {
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
            YWSaNF.LOGGER.error("Error trying to apply after-place for structure with Leukbox at {}!", structureOrigin.toString());
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

    public static void placeStoredStructureStables(IntermediateStructureStorage intermediateStorage, int currentSpawnTime, BlockPos pos, ServerLevel level) {
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
                                    setBlock(level, mutableBlockPos, newState, true);
                                    tryUpdateSelf(level, mutableBlockPos, newState);
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
                                    setBlock(level, mutableBlockPos, feastingTAR, true);
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public static void placeStoredStructureSpecials(IntermediateStructureStorage intermediateStorage, ServerLevel level) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        // place blocks
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
                                    setBlock(level, mutableBlockPos, state, false);
                                }
                            }
                        }
                    }
                }
            }
        });
        // place block entities
        intermediateStorage.forEachBlockEntity(((blockPos, blockEntity) -> {
            BlockState state = intermediateStorage.getFragileBlockState(blockPos);
            BlockState entityState = blockEntity.getBlockState();
            if(state.getBlock().equals(entityState.getBlock())) {
                level.getChunkAt(blockPos).addAndRegisterBlockEntity(blockEntity);
            }
        }));
        // update blocks
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
                            mutableBlockPos.set(mx + x, my + y, mz + z);
                            BlockState newState = level.getLevel().getChunkAt(mutableBlockPos).getBlockState(mutableBlockPos);
                            if(state.equals(newState)) {
                                level.blockUpdated(mutableBlockPos, state.getBlock());
                                if (state.hasAnalogOutputSignal()) {
                                    level.updateNeighbourForOutputSignal(mutableBlockPos, state.getBlock());
                                }
                                tryUpdateSelf(level, mutableBlockPos, state);
                            }
                        }
                    }
                }
            }
        });
        // place entities
        intermediateStorage.forEachEntity(level::addFreshEntity);
    }

    public static void setBlock(ServerLevel level, BlockPos pos, BlockState state, boolean updateNeighbors) {
        level.setBlock(pos, state, updateNeighbors ? 3 : 2, 512);
    }

    public static void tryUpdateSelf(ServerLevel level, BlockPos pos, BlockState state) {
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            fluidState.tick(level, pos);
        }

        Block block = state.getBlock();
        if (!(block instanceof LiquidBlock)) {
            BlockState newState = Block.updateFromNeighbourShapes(state, level, pos);
            if (!newState.equals(state)) {
                level.setBlock(pos, newState, 20);
            }
        }
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
        return h * 7.0F + 0.9F * -dy + n * 4.5F;
    }

    public static IntNoise2D generateNoise() {
        RandomSource randomSource = RandomSource.create(12345);
        return IntNoise2D.generateNoise(128, 128, () -> randomSource.nextInt(MAX_NOISE_DELAY));
    }
}
