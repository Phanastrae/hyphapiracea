package phanastrae.hyphapiracea.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.block.HyphalNodeBlock;
import phanastrae.hyphapiracea.block.MiniCircuit;
import phanastrae.hyphapiracea.block.MiniCircuitHolder;
import phanastrae.hyphapiracea.electromagnetism.CircuitNetwork;
import phanastrae.hyphapiracea.electromagnetism.CircuitNode;
import phanastrae.hyphapiracea.electromagnetism.CircuitWire;

public class HyphalNodeBlockEntity extends BlockEntity implements MiniCircuitHolder {

    private boolean[] directionFilled;
    protected final MiniCircuit miniCircuit;
    protected CircuitNode coreNode;

    public HyphalNodeBlockEntity(BlockPos pos, BlockState blockState) {
        super(HyphaPiraceaBlockEntityTypes.HYPHAL_NODE, pos, blockState);

        this.directionFilled = new boolean[]{false, false, false, false, false, false};

        this.miniCircuit = new MiniCircuit();
        this.coreNode = new CircuitNode();
        CircuitNetwork network = new CircuitNetwork();
        coreNode.setNetwork(network);

        for(Direction direction : Direction.values()) {
            boolean open = blockState.getValue(HyphalNodeBlock.PROPERTY_BY_DIRECTION.get(direction));
            if(open) {
                this.addSide(direction);
            }
        }
    }

    public void addSide(Direction direction) {
        int index = direction.get3DDataValue();
        if(!this.directionFilled[index]) {
            this.directionFilled[index] = true;

            CircuitNetwork network = this.coreNode.getNetwork();
            if(network == null) {
                network = new CircuitNetwork();
            }
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
    public MiniCircuit getMiniCircuit(BlockGetter blockGetter, BlockPos pos, BlockState state, Direction side) {
        return this.miniCircuit;
    }
}
