package phanastrae.ywsanf.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.block.MiniCircuit;
import phanastrae.ywsanf.block.MiniCircuitHolder;
import phanastrae.ywsanf.electromagnetism.CircuitNetwork;
import phanastrae.ywsanf.electromagnetism.CircuitNode;
import phanastrae.ywsanf.electromagnetism.CircuitWire;

public class GalvanocarpicBulbBlockEntity extends BlockEntity implements MiniCircuitHolder {

    protected final MiniCircuit miniCircuit;

    public GalvanocarpicBulbBlockEntity(BlockPos pos, BlockState blockState) {
        super(YWSaNFBlockEntityTypes.GALVANOCARPIC_BULB, pos, blockState);

        this.miniCircuit = new MiniCircuit();
        CircuitNetwork network = new CircuitNetwork();
        CircuitNode coreNode = new CircuitNode();
        coreNode.setNetwork(network);
        for(Direction direction : Direction.values()) {
            CircuitNode sideNode = new CircuitNode();
            sideNode.setNetwork(network);

            CircuitWire sideWire = new CircuitWire(coreNode, sideNode, this.getInternalResistance(), 0);
            network.addWire(sideWire);
            this.miniCircuit.addInternalWire(sideWire);

            this.miniCircuit.setNode(direction, sideNode);
        }
    }

    public float getInternalResistance() {
        return 0.01F;
    }

    @Override
    public void setRemoved() {
        this.miniCircuit.onRemoved();
        super.setRemoved();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        this.miniCircuit.onUnremoved();
    }

    @Override
    @Nullable
    public MiniCircuit getMiniCircuit(Level level, BlockPos pos, BlockState state, Direction side) {
        return this.miniCircuit;
    }
}
