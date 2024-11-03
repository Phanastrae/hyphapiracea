package phanastrae.hyphapiracea.structure.leubox_stages;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import phanastrae.hyphapiracea.structure.IntermediateStructureStorage;

public class InsufficientMagneticFieldStage extends AbstractLeukboxStage {

    private final IntermediateStructureStorage intermediateStructureStorage;
    private final int currentSpawnTime;
    private final int minSpawnTime;

    public InsufficientMagneticFieldStage(BlockPos leukboxPos, IntermediateStructureStorage intermediateStructureStorage, int currentSpawnTime, int minSpawnTime) {
        super(leukboxPos, LeukboxStage.INSUFFICIENT_MAGNETIC_FIELD);
        this.intermediateStructureStorage = intermediateStructureStorage;
        this.currentSpawnTime = currentSpawnTime;
        this.minSpawnTime = minSpawnTime;
    }

    @Override
    public AbstractLeukboxStage advanceStage(ServerLevel serverLevel, Vec3 magneticField, float maxOperatingRadius, float minOperatingTesla) {
        // wait for the magnetic field to be high enough
        if(magneticField.length() >= minOperatingTesla) {
            return new PlaceBlocksStage(this.leukboxPos, this.intermediateStructureStorage, currentSpawnTime, minSpawnTime);
        } else {
            return this;
        }
    }
}
