package phanastrae.ywsanf.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class VoltmeterBlockEntity extends AbstractTwoSidedChargeSacBlockEntity {

    public VoltmeterBlockEntity(BlockPos pos, BlockState blockState) {
        super(YWSaNFBlockEntityTypes.VOLTMETER_BLOCK, pos, blockState);
    }

    @Override
    public float getInternalResistance() {
        return 10000F;
    }
}
