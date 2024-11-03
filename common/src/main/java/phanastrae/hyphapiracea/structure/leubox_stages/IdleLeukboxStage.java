package phanastrae.hyphapiracea.structure.leubox_stages;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class IdleLeukboxStage extends AbstractLeukboxStage {

    public IdleLeukboxStage(BlockPos leukboxPos) {
        super(leukboxPos, LeukboxStage.IDLE);
    }

    @Override
    public AbstractLeukboxStage advanceStage(ServerLevel serverLevel, Vec3 magneticField, float maxOperatingRadius, float minOperatingTesla) {
        return this;
    }
}
