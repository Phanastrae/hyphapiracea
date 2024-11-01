package phanastrae.ywsanf.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import phanastrae.ywsanf.electromagnetism.CircuitNode;

public class GalvanocarpicBulbBlockEntity extends BlockEntity {

    private final CircuitNode circuitNode;

    public GalvanocarpicBulbBlockEntity(BlockPos pos, BlockState blockState) {
        super(YWSaNFBlockEntityTypes.GALVANOCARPIC_BULB, pos, blockState);

        this.circuitNode = new CircuitNode();
    }

    public CircuitNode getCircuitNode() {
        return this.circuitNode;
    }
}
