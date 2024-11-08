package phanastrae.hyphapiracea.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import phanastrae.hyphapiracea.block.CreativeCellBlock;
import phanastrae.hyphapiracea.electromagnetism.CircuitNetwork;

public class CreativeCellBlockEntity extends StormsapCellBlockEntity {

    protected boolean powered;

    public CreativeCellBlockEntity(BlockPos pos, BlockState state) {
        super(HyphaPiraceaBlockEntityTypes.CREATIVE_CELL, pos, state);

        if(state.hasProperty(CreativeCellBlock.POWERED)) {
            this.powered = state.getValue(CreativeCellBlock.POWERED);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CreativeCellBlockEntity blockEntity) {
        AbstractTwoSidedCircuitComponentBlockEntity.serverTick(level, pos, state, blockEntity);

        CircuitNetwork network = blockEntity.wire.getStartNode().getNetwork();
        if(!blockEntity.active && !blockEntity.powered) {
            blockEntity.active = true;
            // wire flows from positive start to negative end, emf here refers to push from start to end, we want push from negative (end) to positive (start) hence negative emf
            blockEntity.wire.setEmf(-blockEntity.getGeneratedVoltage());
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

    public void setPowered(boolean powered) {
        this.powered = powered;
    }
}
