package phanastrae.ywsanf.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import phanastrae.ywsanf.YWSaNF;

import java.util.Optional;

public class StructurePlacement {

    public static boolean placeStructure(ServerLevel serverLevel, BlockPos pos) {
        ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, ResourceLocation.withDefaultNamespace("bastion_remnant"));

        Optional<Registry<Structure>> structureRegistryOptional = serverLevel.registryAccess().registry(Registries.STRUCTURE);
        if (structureRegistryOptional.isEmpty()) {
            return false;
        }

        Optional<Holder.Reference<Structure>> structureReferenceOptional = structureRegistryOptional.get().getHolder(key);
        if (structureReferenceOptional.isEmpty()) {
            return false;
        }

        Structure structure = structureReferenceOptional.get().value();
        StructureStart structureStart = getStructureStart(serverLevel, structure, pos);

        if (!structureStart.isValid()) {
            return false;
        }

        Optional<IntermediateStructureStorage> storageOptional = fillStructureStorage(serverLevel, structureStart);
        if(storageOptional.isEmpty()) {
            return false;
        }

        IntermediateStructureStorage storage = storageOptional.get();
        placeStoredStructure(storage, serverLevel);
        return true;
    }

    public static Optional<IntermediateStructureStorage> fillStructureStorage(ServerLevel serverLevel, StructureStart structureStart) {
        BoundingBox boundingbox = structureStart.getBoundingBox();
        ChunkPos startPos = new ChunkPos(SectionPos.blockToSectionCoord(boundingbox.minX()), SectionPos.blockToSectionCoord(boundingbox.minZ()));
        ChunkPos endPos = new ChunkPos(SectionPos.blockToSectionCoord(boundingbox.maxX()), SectionPos.blockToSectionCoord(boundingbox.maxZ()));
        if(!checkLoaded(serverLevel, startPos, endPos)) {
            return Optional.empty();
        }

        IntermediateGenLevel intermediateGenLevel = new IntermediateGenLevel(serverLevel);
        try {
            ChunkPos.rangeClosed(startPos, endPos)
                    .forEach(
                            chunkPos -> structureStart.placeInChunk(
                                    intermediateGenLevel,
                                    serverLevel.structureManager(),
                                    serverLevel.getChunkSource().getGenerator(),
                                    serverLevel.getRandom(),
                                    new BoundingBox(
                                            chunkPos.getMinBlockX(),
                                            serverLevel.getMinBuildHeight(),
                                            chunkPos.getMinBlockZ(),
                                            chunkPos.getMaxBlockX(),
                                            serverLevel.getMaxBuildHeight(),
                                            chunkPos.getMaxBlockZ()
                                    ),
                                    chunkPos
                            )
                    );
        } catch (Exception e) {
            YWSaNF.LOGGER.error("Error trying to generate structure!"); // TODO consider changing this
            return Optional.empty();
        }

        return Optional.of(intermediateGenLevel.getStorage());
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

    public static StructureStart getStructureStart(ServerLevel serverLevel, Structure structure, BlockPos pos) {
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

    private static boolean checkLoaded(ServerLevel level, ChunkPos start, ChunkPos end) {
        if (ChunkPos.rangeClosed(start, end).anyMatch(chunkPos -> !level.isLoaded(chunkPos.getWorldPosition()))) {
            return false;
        }

        return true;
    }
}
