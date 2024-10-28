package phanastrae.ywsanf.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import phanastrae.ywsanf.electromagnetism.ChargeSac;

public class StormsapCellBlockEntity extends AbstractTwoSidedChargeSacBlockEntity {

    public StormsapCellBlockEntity(BlockPos pos, BlockState blockState) {
        super(YWSaNFBlockEntityTypes.STORMSAP_CELL, pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StormsapCellBlockEntity blockEntity) {
        long chargeDelta = ChargeSac.getChargeDeltaMilliCoulombs(blockEntity.secondaryChargeSac, blockEntity.primaryChargeSac, blockEntity.getInternalResistance());

        double voltage = blockEntity.getGeneratedVoltage();
        double resistance = blockEntity.getInternalResistance();
        double currentAmps = voltage / resistance;
        double currentMilliCoulombsPerTick = currentAmps * 1000 / 20;

        long c = (long) Mth.ceil(Math.abs(currentMilliCoulombsPerTick)) * Mth.sign(currentMilliCoulombsPerTick);
        c *= -1;
        c = ChargeSac.limitDeltaCharge(c, blockEntity.secondaryChargeSac, blockEntity.primaryChargeSac);

        chargeDelta += c;

        if(chargeDelta != 0) {
            blockEntity.pushCharge(chargeDelta);
        }
    }

    public float getGeneratedVoltage() {
        return 12F;
    }
}
