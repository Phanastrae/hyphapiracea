package phanastrae.hyphapiracea.structure.leubox_stages;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class CompletedStage extends AbstractLeukboxStage {

    public CompletedStage(BlockPos leukboxPos) {
        super(leukboxPos, LeukboxStage.COMPLETED);
    }

    @Override
    public AbstractLeukboxStage advanceStage(ServerLevel serverLevel, Vec3 magneticField, float maxOperatingRadius, float minOperatingTesla) {
        return new IdleLeukboxStage(this.leukboxPos);
    }
}
