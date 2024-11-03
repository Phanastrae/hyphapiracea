package phanastrae.hyphapiracea.structure.leubox_stages;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class ErrorStage extends AbstractLeukboxStage implements ErrorIdHolder {

    private final String errorId;

    public ErrorStage(BlockPos leukboxPos, String errorId) {
        super(leukboxPos, LeukboxStage.ERROR);

        this.errorId = errorId;
    }

    @Override
    public String getErrorId() {
        return this.errorId;
    }

    @Override
    public ErrorStage advanceStage(ServerLevel serverLevel, Vec3 magneticField, float maxOperatingRadius, float minOperatingTesla) {
        return this;
    }
}
