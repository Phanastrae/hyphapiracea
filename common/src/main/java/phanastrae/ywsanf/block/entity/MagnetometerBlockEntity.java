package phanastrae.ywsanf.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import phanastrae.ywsanf.world.YWSaNFLevelAttachment;

public class MagnetometerBlockEntity extends BlockEntity {

    private int timer = 0;
    private int lastComparatorOutput = -1;

    public MagnetometerBlockEntity(BlockPos pos, BlockState blockState) {
        super(YWSaNFBlockEntityTypes.MAGNETOMETER_BLOCK, pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MagnetometerBlockEntity blockEntity) {
        blockEntity.timer--;
        if(blockEntity.timer <= 0) {
            blockEntity.timer = 10;

            int comparatorOutput = blockEntity.calculateComparatorOutput();
            if(comparatorOutput != blockEntity.lastComparatorOutput) {
                blockEntity.lastComparatorOutput = comparatorOutput;
                blockEntity.setChanged();
            }
        }
    }

    public int calculateComparatorOutput() {
        Vec3 magneticField = YWSaNFLevelAttachment.getAttachment(level).getMagneticFieldAtPosition(this.getBlockPos().getCenter());
        double magneticFieldStrength = magneticField.length();

        int comparatorOutput = 0;
        double strengthToBeat = 2.5E-9;
        for(int i = 0; i < 15; i++) {
            if(magneticFieldStrength >= strengthToBeat) {
                strengthToBeat *= 2;
                comparatorOutput += 1;
            } else {
                break;
            }
        }

        return comparatorOutput;
    }

    public int getComparatorOutput() {
        if(this.lastComparatorOutput == -1) {
            this.lastComparatorOutput = this.calculateComparatorOutput();
            this.setChanged();
        }

        return this.lastComparatorOutput;
    }
}
