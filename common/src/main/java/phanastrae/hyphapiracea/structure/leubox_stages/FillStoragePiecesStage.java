package phanastrae.hyphapiracea.structure.leubox_stages;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.structure.IntermediateGenLevel;
import phanastrae.hyphapiracea.structure.IntermediateStructureStorage;

import java.util.LinkedList;
import java.util.Queue;

import static phanastrae.hyphapiracea.structure.StructurePlacer.checkLoadedOrOutOfRange;

public class FillStoragePiecesStage extends AbstractLeukboxStage {

    private final @Nullable Structure structure;
    private final PiecesContainer structurePieces;
    private final BoundingBox boundingBox;
    private final BlockPos structureOrigin;
    private final Queue<StructurePiece> unplacedPieces;
    private final IntermediateStructureStorage intermediateStructureStorage;

    public FillStoragePiecesStage(BlockPos leukboxPos, BlockPos structureOrigin, LinkedList<StructurePiece> unplacedPieces, @Nullable Structure structure, PiecesContainer piecesContainer, BoundingBox boundingBox) {
        super(leukboxPos, LeukboxStage.FILL_STORAGE_PIECES);

        this.unplacedPieces = unplacedPieces;
        this.structure = structure;
        this.structurePieces = piecesContainer;
        this.boundingBox = boundingBox;
        this.structureOrigin = structureOrigin;
        this.intermediateStructureStorage = new IntermediateStructureStorage();

        this.requiredOperations = this.structurePieces.pieces().size();
    }

    @Override
    public AbstractLeukboxStage advanceStage(ServerLevel serverLevel, Vec3 magneticField, float maxOperatingRadius, float minOperatingTesla) {
        // TODO this may be able to be split further across time for jigsaw structure unplacedPieces using ListPoolElement
        // fill structure storage from structureStart, adding the unplacedPieces one by one until done
        if(!this.unplacedPieces.isEmpty()) {
            StructurePiece nextPiece = this.unplacedPieces.remove();

            if(!this.placeStructurePiece(
                    serverLevel,
                    nextPiece,
                    maxOperatingRadius
            )) {
                return this.getError("chunk_not_loaded");
            }
        }

        if(this.unplacedPieces.isEmpty()) {
            return new FillStorageAfterStage(this.leukboxPos, this.intermediateStructureStorage, this.structure, this.structurePieces, this.boundingBox);
        } else {
            return this;
        }
    }

    public boolean placeStructurePiece(ServerLevel serverLevel, StructurePiece structurePiece, float maxOperatingRadius) {
        BoundingBox boundingBox = structurePiece.getBoundingBox();
        if(!checkLoadedOrOutOfRange(serverLevel, boundingBox, this.leukboxPos, maxOperatingRadius)) {
            return false;
        }

        IntermediateGenLevel intermediateGenLevel = new IntermediateGenLevel(this.intermediateStructureStorage, serverLevel);
        StructureManager structureManager = serverLevel.structureManager();
        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        RandomSource randomSource = serverLevel.getRandom();

        ChunkPos startPos = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.minX()), SectionPos.blockToSectionCoord(boundingBox.minZ()));
        ChunkPos endPos = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.maxX()), SectionPos.blockToSectionCoord(boundingBox.maxZ()));
        try {
            ChunkPos.rangeClosed(startPos, endPos)
                    .forEach(
                            chunkPos -> {
                                if(serverLevel.isLoaded(chunkPos.getWorldPosition())) {
                                    BoundingBox chunkBox =
                                            new BoundingBox(
                                                    chunkPos.getMinBlockX(),
                                                    serverLevel.getMinBuildHeight(),
                                                    chunkPos.getMinBlockZ(),
                                                    chunkPos.getMaxBlockX(),
                                                    serverLevel.getMaxBuildHeight(),
                                                    chunkPos.getMaxBlockZ()
                                            );
                                    structurePiece.postProcess(
                                            intermediateGenLevel,
                                            structureManager,
                                            chunkGenerator,
                                            randomSource,
                                            chunkBox,
                                            chunkPos,
                                            this.structureOrigin
                                    );
                                }
                            }
                    );
        } catch (Exception e) {
            HyphaPiracea.LOGGER.error("Error trying to place piece for structure with Leukbox at {}!", this.leukboxPos.toString());
            return false;
        }

        return true;
    }
}
