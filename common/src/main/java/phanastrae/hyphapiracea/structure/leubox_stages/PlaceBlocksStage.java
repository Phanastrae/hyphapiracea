package phanastrae.hyphapiracea.structure.leubox_stages;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import phanastrae.hyphapiracea.block.HyphaPiraceaBlocks;
import phanastrae.hyphapiracea.electromagnetism.WorldWireField;
import phanastrae.hyphapiracea.structure.IntermediateStructureStorage;
import phanastrae.hyphapiracea.world.HyphaPiraceaLevelAttachment;

import static phanastrae.hyphapiracea.structure.StructurePlacer.*;

public class PlaceBlocksStage extends AbstractLeukboxStage implements SpawnTimeHolder {

    private final IntermediateStructureStorage intermediateStructureStorage;
    private int currentSpawnTime;
    private final int minSpawnTime;

    public PlaceBlocksStage(BlockPos leukboxPos, IntermediateStructureStorage intermediateStructureStorage, int currentSpawnTime, int minSpawnTime) {
        super(leukboxPos, LeukboxStage.PLACE_BLOCKS);

        this.intermediateStructureStorage = intermediateStructureStorage;
        this.currentSpawnTime = currentSpawnTime;
        this.minSpawnTime = minSpawnTime;

        this.requiredOperations = this.currentSpawnTime - this.minSpawnTime + 2;
    }

    @Override
    public int getCurrentSpawnTime() {
        return this.currentSpawnTime;
    }

    @Override
    public int getMinSpawnTime() {
        return this.minSpawnTime;
    }

    @Override
    public AbstractLeukboxStage advanceStage(ServerLevel serverLevel, Vec3 magneticField, float maxOperatingRadius, float minOperatingTesla) {
        // place stable structure storage
        this.placeStoredStructureStables(serverLevel, magneticField, maxOperatingRadius, minOperatingTesla);

        if(this.currentSpawnTime < this.minSpawnTime) {
            return new PlaceSpecialsStage(this.leukboxPos, this.intermediateStructureStorage);
        } else {
            this.currentSpawnTime--;
            return this;
        }
    }

    public void placeStoredStructureStables(ServerLevel level, Vec3 magneticField, float maxOperatingRadius, float minOperatingTesla) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockState piraceaticTAR = HyphaPiraceaBlocks.PIRACEATIC_TAR.defaultBlockState();
        HyphaPiraceaLevelAttachment levelAttachment = HyphaPiraceaLevelAttachment.getAttachment(level);

        this.intermediateStructureStorage.forEachContainer((sectionPos, boxedContainer) -> {
            WorldWireField.SectionInfo worldWireSectionInfo = levelAttachment.getWorldWireField().getSectionInfoForPosition(sectionPos);

            BoundingBox box = boxedContainer.getBox();
            if(box == null) return;

            int mx = sectionPos.minBlockX();
            int my = sectionPos.minBlockY();
            int mz = sectionPos.minBlockZ();
            if (!isBoxInTimeRange(box, this.leukboxPos.subtract(new Vec3i(mx, my, mz)), this.currentSpawnTime)) return;

            for (int x = box.minX(); x <= box.maxX(); x++) {
                int dx = mx + x - this.leukboxPos.getX();
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    int dz = mz + z - this.leukboxPos.getZ();

                    double r = calcHorizontalFactor(dx, dz);
                    double noise = NOISE.get(dx, dz);

                    for (int y = box.minY(); y <= box.maxY(); y++) {
                        int dy = my + y - this.leukboxPos.getY();

                        double t = calcSpawnTime(r, dy, noise);
                        if(tInRange(t, currentSpawnTime)) {
                            BlockState newState = boxedContainer.get(x, y, z);
                            if (!newState.is(Blocks.STRUCTURE_VOID)) {
                                mutableBlockPos.set(mx + x, my + y, mz + z);

                                if(isPositionInRange(mutableBlockPos, this.leukboxPos, maxOperatingRadius)) {
                                    BlockState oldState = level.getBlockState(mutableBlockPos);
                                    if (isStateSubsumed(oldState)) {
                                        spawnDissolveParticles(level, mutableBlockPos, 0.3F);
                                        setBlock(level, mutableBlockPos, newState, true);
                                        tryUpdateSelf(level, mutableBlockPos, newState);
                                    }
                                }
                            }
                        } else if(tInRange(t + CONVERSION_DELAY, currentSpawnTime)) {
                            BlockState newState = boxedContainer.get(x, y, z);
                            if (!newState.is(Blocks.STRUCTURE_VOID)) {
                                mutableBlockPos.set(mx + x, my + y, mz + z);

                                if(isPositionInRange(mutableBlockPos, this.leukboxPos, maxOperatingRadius - noise)) {
                                    BlockState oldState = level.getBlockState(mutableBlockPos);
                                    if (newState.isAir() && oldState.isAir()) {
                                        continue;
                                    }
                                    if (isStateFeastable(oldState, level, mutableBlockPos)) {
                                        Vec3 localMagneticField = levelAttachment.getMagneticFieldAtPosition(mutableBlockPos.getCenter(), worldWireSectionInfo, true);
                                        if (canOperateUnderFields(magneticField, localMagneticField, minOperatingTesla)) {
                                            spawnConsumeParticles(level, mutableBlockPos, oldState, 0.3F);
                                            setBlock(level, mutableBlockPos, piraceaticTAR, true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
    }
}
