package phanastrae.hyphapiracea.structure.leubox_stages;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.block.LeukboxBlock;
import phanastrae.hyphapiracea.mixin.StructureStartAccessor;
import phanastrae.hyphapiracea.structure.StructureType;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class GetStructureStartStage extends AbstractLeukboxStage {

    private final @Nullable Structure structure;
    private final ResourceLocation structureId;
    private final StructureType structureType;
    private final boolean rotateStructure;

    public GetStructureStartStage(BlockPos leukboxPos, @Nullable Structure structure, ResourceLocation structureId, StructureType structureType, boolean rotateStructure) {
        super(leukboxPos, LeukboxStage.GET_STRUCTURE_START);
        this.structure = structure;
        this.structureId = structureId;
        this.structureType = structureType;
        this.rotateStructure = rotateStructure;
    }

    @Override
    public AbstractLeukboxStage advanceStage(ServerLevel serverLevel, Vec3 magneticField, float maxOperatingRadius, float minOperatingTesla) {
        // TODO this may be able to be split further across time for jigsaw structures
        // get structure start from structure
        long levelSeed = serverLevel.getSeed();
        long secondarySeed = serverLevel.getRandom().nextLong();
        long seed = levelSeed ^ secondarySeed;

        Optional<PiecesContainer> piecesContainerOptional = this.getPiecesContainer(serverLevel, seed);
        if (piecesContainerOptional.isPresent()) {
            PiecesContainer piecesContainer = piecesContainerOptional.get();
            List<StructurePiece> pieces = piecesContainer.pieces();
            BoundingBox box = piecesContainer.calculateBoundingBox();

            // get structure base position
            BlockPos structureBase;
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
                    this.structure,
                    piecesContainer,
                    box
            );
        } else {
            return this.getError("invalid_structure_start");
        }
    }

    public Optional<PiecesContainer> getPiecesContainer(ServerLevel serverLevel, long seed) {
        if(this.structureType == StructureType.STRUCTURE && this.structure != null) {
            StructureStart structureStart = getStructureStart(this.structure, this.leukboxPos, serverLevel, seed);
            if(structureStart.isValid()) {
                return Optional.of(((StructureStartAccessor)(Object)structureStart).getPieceContainer());
            }
        }

        if(this.structureType == StructureType.TEMPLATE) {
            StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();
            Optional<StructureTemplate> optional;
            try {
                optional = structureTemplateManager.get(this.structureId);
            } catch (ResourceLocationException resourcelocationexception) {
                HyphaPiracea.LOGGER.info("Failed to find structure template with id {}!", this.structureId);
                return Optional.empty();
            }

            if(optional.isPresent()) {
                StructureTemplate structuretemplate = optional.get();

                Mirror mirror = Mirror.NONE;
                Rotation rotation;
                if(this.rotateStructure) {
                    // TODO implement rotateStructure on non-template structures, if possible and something that makes sense?
                    rotation = Rotation.getRandom(serverLevel.getRandom());
                } else {
                    rotation = Rotation.NONE;
                }
                StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings().setMirror(mirror).setRotation(rotation);
                StructurePiece structurePiece = new PoolElementStructurePiece(
                        structureTemplateManager,
                        SinglePoolElement.single(this.structureId.toString()).apply(StructureTemplatePool.Projection.RIGID),
                        this.leukboxPos,
                        0,
                        rotation,
                        structuretemplate.getBoundingBox(structurePlaceSettings, this.leukboxPos),
                        LiquidSettings.IGNORE_WATERLOGGING
                );
                StructurePiecesBuilder structurePiecesBuilder = new StructurePiecesBuilder();
                structurePiecesBuilder.addPiece(structurePiece);
                return Optional.of(structurePiecesBuilder.build());
            } else {
                return Optional.empty();
            }
        }

        return Optional.empty();
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
