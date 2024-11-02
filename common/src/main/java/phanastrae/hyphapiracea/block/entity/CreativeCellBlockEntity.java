package phanastrae.hyphapiracea.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import phanastrae.hyphapiracea.electromagnetism.CircuitNetwork;

public class CreativeCellBlockEntity extends StormsapCellBlockEntity {
    public CreativeCellBlockEntity(BlockPos pos, BlockState state) {
        super(HyphaPiraceaBlockEntityTypes.CREATIVE_CELL, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CreativeCellBlockEntity blockEntity) {
        AbstractTwoSidedChargeSacBlockEntity.serverTick(level, pos, state, blockEntity);

        CircuitNetwork network = blockEntity.wire.getStartNode().getNetwork();
        if(!blockEntity.active && !blockEntity.powered) {
            blockEntity.active = true;
            blockEntity.wire.setEmf(blockEntity.getGeneratedVoltage());
            if(network != null) {
                network.markNeedsUpdate();
            }
        } else if(blockEntity.active && blockEntity.powered) {
            blockEntity.active = false;
            blockEntity.wire.setEmf(0);
            if(network != null) {
                network.markNeedsUpdate();
            }
        }
    }
}
