package phanastrae.hyphapiracea.structure.leubox_stages;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class IntroStage extends AbstractLeukboxStage {

    public IntroStage(BlockPos leukboxPos, LeukboxStage stage) {
        super(leukboxPos, stage);
    }

    @Override
    public AbstractLeukboxStage advanceStage(ServerLevel serverLevel, Vec3 magneticField, float maxOperatingRadius, float minOperatingTesla) {
        switch(this.getStage()) {
            case INTRO_SCREEN -> {
                return new IntroStage(this.leukboxPos, LeukboxStage.INTRO_INITIALISING);
            }
            case INTRO_INITIALISING -> {
                return new IntroStage(this.leukboxPos, LeukboxStage.INTRO_LOADING);
            }
            case INTRO_LOADING -> {
                return new IntroStage(this.leukboxPos, LeukboxStage.INTRO_WELCOME);
            }
            default -> {
                return new IntroStage(this.leukboxPos, LeukboxStage.IDLE);
            }
        }
    }
}
