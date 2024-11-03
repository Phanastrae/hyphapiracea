package phanastrae.hyphapiracea.structure.leubox_stages;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.phys.Vec3;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.structure.BoxedContainer;
import phanastrae.hyphapiracea.structure.IntermediateGenLevel;
import phanastrae.hyphapiracea.structure.IntermediateStructureStorage;
import phanastrae.hyphapiracea.structure.StructurePlacer;

import java.util.LinkedList;

public class FillStorageAfterStage extends AbstractLeukboxStage {

    private final Structure structure;
    private final IntermediateStructureStorage intermediateStructureStorage;
    private final BoundingBox boundingBox;
    private final PiecesContainer piecesContainer;
    private final BlockPos structureOrigin;

    public FillStorageAfterStage(BlockPos leukboxPos, IntermediateStructureStorage intermediateStructureStorage, Structure structure, PiecesContainer piecesContainer, BoundingBox boundingBox, BlockPos structureOrigin) {
        super(leukboxPos, LeukboxStage.FILL_STORAGE_AFTER);

        this.structure = structure;
        this.intermediateStructureStorage = intermediateStructureStorage;
        this.boundingBox = boundingBox;
        this.piecesContainer = piecesContainer;
        this.structureOrigin = structureOrigin;
    }

    @Override
    public AbstractLeukboxStage advanceStage(ServerLevel serverLevel, Vec3 magneticField, float maxOperatingRadius, float minOperatingTesla) {
        // fill structure storage from structureStart, doing the after place stuff
        // this isn't split across ticks, as it iterates through chunk sections whilst also depending on the world state, which means splitting it across ticks would give visible chunk borders
        if(!this.fillAfterPlace(
                serverLevel, maxOperatingRadius
        )) {
            return this.getError("chunk_not_loaded");
        } else {
            LinkedList<Pair<SectionPos, BoxedContainer>> list = new LinkedList<>();
            this.intermediateStructureStorage.forEachContainer(((sectionPos, boxedContainer) -> list.add(Pair.of(sectionPos, boxedContainer))));
            return new PostProcessStage(this.leukboxPos, this.intermediateStructureStorage, this.boundingBox, list, list.size());
        }
    }

    public boolean fillAfterPlace(ServerLevel serverLevel, float maxOperatingRadius) {
        if(!StructurePlacer.checkLoadedOrOutOfRange(serverLevel, this.boundingBox, this.leukboxPos, maxOperatingRadius)) {
            return false;
        }

        IntermediateGenLevel intermediateGenLevel = new IntermediateGenLevel(this.intermediateStructureStorage, serverLevel);
        int minHeight = serverLevel.getMinBuildHeight();
        int maxHeight = serverLevel.getMaxBuildHeight();
        StructureManager structureManager = serverLevel.structureManager();
        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        RandomSource randomSource = serverLevel.getRandom();

        ChunkPos startPos = new ChunkPos(SectionPos.blockToSectionCoord(this.boundingBox.minX()), SectionPos.blockToSectionCoord(this.boundingBox.minZ()));
        ChunkPos endPos = new ChunkPos(SectionPos.blockToSectionCoord(this.boundingBox.maxX()), SectionPos.blockToSectionCoord(this.boundingBox.maxZ()));
        try {
            ChunkPos.rangeClosed(startPos, endPos)
                    .forEach(
                            chunkPos -> {
                                if(serverLevel.isLoaded(chunkPos.getWorldPosition())) {
                                    afterPlaceInChunk(
                                            this.structure,
                                            this.piecesContainer,
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
                                            chunkPos);
                                }
                            }
                    );
        } catch (Exception e) {
            HyphaPiracea.LOGGER.error("Error trying to apply after-place for structure {} with Leukbox at {}!", this.structure.toString(), this.structureOrigin.toString());
            return false;
        }

        return true;
    }

    public static void afterPlaceInChunk(Structure structure, PiecesContainer piecesContainer, WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos) {
        if (!piecesContainer.isEmpty()) {
            structure.afterPlace(level, structureManager, generator, random, chunkBox, chunkPos, piecesContainer);
        }
    }
}
