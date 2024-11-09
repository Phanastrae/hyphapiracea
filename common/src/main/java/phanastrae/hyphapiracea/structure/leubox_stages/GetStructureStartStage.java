package phanastrae.hyphapiracea.structure.leubox_stages;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.Vec3;
import phanastrae.hyphapiracea.block.LeukboxBlock;
import phanastrae.hyphapiracea.mixin.StructureStartAccessor;

import java.util.LinkedList;
import java.util.List;

public class GetStructureStartStage extends AbstractLeukboxStage {

    private final Structure structure;

    public GetStructureStartStage(BlockPos leukboxPos, Structure structure) {
        super(leukboxPos, LeukboxStage.GET_STRUCTURE_START);
        this.structure = structure;
    }

    @Override
    public AbstractLeukboxStage advanceStage(ServerLevel serverLevel, Vec3 magneticField, float maxOperatingRadius, float minOperatingTesla) {
        // TODO this may be able to be split further across time for jigsaw structures
        // get structure start from structure
        long levelSeed = serverLevel.getSeed();
        long secondarySeed = serverLevel.getRandom().nextLong();
        long seed = levelSeed ^ secondarySeed;
        StructureStart structureStart = getStructureStart(this.structure, this.leukboxPos, serverLevel, seed);
        if (structureStart.isValid()) {
            // get structure base position
            BoundingBox box = structureStart.getBoundingBox();

            BlockPos structureBase;
            List<StructurePiece> pieces = structureStart.getPieces();
            if(pieces.isEmpty()) {
                BlockPos boxCenter = box.getCenter();
                structureBase = new BlockPos(boxCenter.getX(), box.minY(), boxCenter.getZ());
            } else {
                BoundingBox box2 = pieces.getFirst().getBoundingBox();
                BlockPos boxCenter = box2.getCenter();
                structureBase = new BlockPos(boxCenter.getX(), box2.minY(), boxCenter.getZ());
            }

            // offset box

            int yOffset = 0;
            BlockState leukboxState = serverLevel.getBlockState(this.leukboxPos);
            if(leukboxState.hasProperty(LeukboxBlock.FACING)) {
                Direction facing = leukboxState.getValue(LeukboxBlock.FACING);
                BlockPos checkPos = leukboxPos.offset(facing.getNormal());
                for(int i = 0; i < 8; i++) {
                    if(serverLevel.getBlockState(checkPos.below()).canBeReplaced()) {
                        yOffset--;
                        checkPos = checkPos.below();
                    }
                }
            }
            Vec3i offset = this.leukboxPos.subtract(structureBase).offset(0, yOffset, 0);

            pieces.forEach(piece -> piece.move(offset.getX(), offset.getY(), offset.getZ()));
            box = box.moved(offset.getX(), offset.getY(), offset.getZ());
            structureBase = structureBase.offset(offset);

            return new FillStoragePiecesStage(
                    this.leukboxPos,
                    structureBase,
                    new LinkedList<>(pieces),
                    structureStart.getStructure(),
                    ((StructureStartAccessor)(Object)structureStart).getPieceContainer(),
                    box
            );
        } else {
            return this.getError("invalid_structure_start");
        }
    }

    public static StructureStart getStructureStart(Structure structure, BlockPos pos, ServerLevel serverLevel, long seed) {
        ChunkGenerator chunkgenerator = serverLevel.getChunkSource().getGenerator();
        return structure.generate(
                serverLevel.registryAccess(),
                chunkgenerator,
                chunkgenerator.getBiomeSource(),
                serverLevel.getChunkSource().randomState(),
                serverLevel.getStructureManager(),
                seed,
                new ChunkPos(pos),
                0,
                serverLevel,
                biome -> true
        );
    }
}
