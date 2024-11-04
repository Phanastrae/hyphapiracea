package phanastrae.hyphapiracea.structure.leubox_stages;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import phanastrae.hyphapiracea.electromagnetism.WorldWireField;
import phanastrae.hyphapiracea.structure.IntermediateStructureStorage;
import phanastrae.hyphapiracea.world.HyphaPiraceaLevelAttachment;

import static phanastrae.hyphapiracea.structure.StructurePlacer.*;

public class PlaceSpecialsStage extends AbstractLeukboxStage {

    private final IntermediateStructureStorage intermediateStructureStorage;

    public PlaceSpecialsStage(BlockPos leukboxPos, IntermediateStructureStorage intermediateStructureStorage) {
        super(leukboxPos, LeukboxStage.PLACE_SPECIALS);

        this.intermediateStructureStorage = intermediateStructureStorage;
    }

    @Override
    public AbstractLeukboxStage advanceStage(ServerLevel serverLevel, Vec3 magneticField, float maxOperatingRadius, float minOperatingTesla) {
        // place fragile structure storage, block entities, entities
        placeStoredStructureSpecials(serverLevel, magneticField, maxOperatingRadius, minOperatingTesla);
        return new CompletedStage(this.leukboxPos);
    }

    public void placeStoredStructureSpecials(ServerLevel level, Vec3 magneticField, float maxOperatingRadius, float minOperatingTesla) {
        HyphaPiraceaLevelAttachment levelAttachment = HyphaPiraceaLevelAttachment.getAttachment(level);

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        // place blocks
        this.intermediateStructureStorage.forEachFragileContainer((sectionPos, boxedContainer) -> {
            WorldWireField.SectionInfo worldWireSectionInfo = levelAttachment.getWorldWireField().getSectionInfoForPosition(sectionPos);

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

                                if(isPositionInRange(mutableBlockPos, this.leukboxPos, maxOperatingRadius)) {
                                    BlockState oldState = level.getBlockState(mutableBlockPos);
                                    if (isStateSubsumed(oldState)) {
                                        Vec3 localMagneticField = levelAttachment.getMagneticFieldAtPosition(mutableBlockPos.getCenter(), worldWireSectionInfo, true);
                                        if (canOperateUnderFields(magneticField, localMagneticField, minOperatingTesla)) {
                                            spawnDissolveParticles(level, mutableBlockPos, 1F);
                                            setBlock(level, mutableBlockPos, state, false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        // place block entities
        this.intermediateStructureStorage.forEachBlockEntity(((blockPos, blockEntity) -> {
            if(isPositionInRange(blockPos, this.leukboxPos, maxOperatingRadius)) {
                BlockState state = level.getBlockState(blockPos);
                BlockState desiredState = this.intermediateStructureStorage.getFragileBlockState(blockPos);
                if(state.equals(desiredState)) {
                    level.getChunkAt(blockPos).addAndRegisterBlockEntity(blockEntity);
                }
            }
        }));
        // update blocks
        this.intermediateStructureStorage.forEachFragileContainer((sectionPos, boxedContainer) -> {
            BoundingBox box = boxedContainer.getBox();
            if(box != null) {
                int mx = sectionPos.minBlockX();
                int my = sectionPos.minBlockY();
                int mz = sectionPos.minBlockZ();
                for (int x = box.minX(); x <= box.maxX(); x++) {
                    for (int y = box.minY(); y <= box.maxY(); y++) {
                        for (int z = box.minZ(); z <= box.maxZ(); z++) {
                            mutableBlockPos.set(mx + x, my + y, mz + z);

                            if(isPositionInRange(mutableBlockPos, this.leukboxPos, maxOperatingRadius)) {
                                BlockState state = boxedContainer.get(x, y, z);
                                BlockState newState = level.getLevel().getChunkAt(mutableBlockPos).getBlockState(mutableBlockPos);
                                if (state.equals(newState)) {
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
            }
        });
        // place entities
        this.intermediateStructureStorage.forEachEntity(entity -> {
            Vec3 entityPos = entity.position();
            int dx = entity.getBlockX() - this.leukboxPos.getX();
            int dz = entity.getBlockZ() - this.leukboxPos.getZ();
            double noise = NOISE.get(dx, dz);
            if(isPositionInRange(entityPos, this.leukboxPos, maxOperatingRadius - noise)) {
                Vec3 entityMagneticField = levelAttachment.getMagneticFieldAtPosition(entityPos, true);
                if (canOperateUnderFields(magneticField, entityMagneticField, minOperatingTesla)) {
                    level.addFreshEntity(entity);
                }
            }
        });
    }
}
