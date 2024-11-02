package phanastrae.hyphapiracea.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.block.MiniCircuit;
import phanastrae.hyphapiracea.block.MiniCircuitHolder;
import phanastrae.hyphapiracea.electromagnetism.CircuitNetwork;
import phanastrae.hyphapiracea.electromagnetism.CircuitNode;
import phanastrae.hyphapiracea.electromagnetism.CircuitWire;

public class HyphalNodeBlockEntity extends BlockEntity implements MiniCircuitHolder {

    protected final MiniCircuit miniCircuit;

    public HyphalNodeBlockEntity(BlockPos pos, BlockState blockState) {
        super(HyphaPiraceaBlockEntityTypes.HYPHAL_NODE, pos, blockState);

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
