package phanastrae.hyphapiracea.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.block.HyphalStemBlock;
import phanastrae.hyphapiracea.block.MiniCircuit;
import phanastrae.hyphapiracea.block.MiniCircuitHolder;
import phanastrae.hyphapiracea.electromagnetism.CircuitNetwork;
import phanastrae.hyphapiracea.electromagnetism.CircuitNode;
import phanastrae.hyphapiracea.electromagnetism.CircuitWire;

public class HyphalStemBlockEntity extends BlockEntity implements MiniCircuitHolder {

    protected final MiniCircuit miniCircuit;

    public HyphalStemBlockEntity(BlockPos pos, BlockState blockState) {
        super(HyphaPiraceaBlockEntityTypes.HYPHAL_STEM, pos, blockState);

        this.miniCircuit = new MiniCircuit();
        CircuitNetwork network = new CircuitNetwork();
        CircuitNode circuitNode1 = new CircuitNode();
        circuitNode1.setNetwork(network);

        CircuitNode circuitNode2 = new CircuitNode();
        circuitNode2.setNetwork(network);

        CircuitWire wire = new CircuitWire(circuitNode1, circuitNode2, this.getInternalResistance(), 0);
        network.addWire(wire);
        this.miniCircuit.addInternalWire(wire);

        if(blockState.hasProperty(HyphalStemBlock.AXIS)) {
            Direction.Axis axis = blockState.getValue(HyphalStemBlock.AXIS);
            for(Direction direction : Direction.values()) {
                if(axis.test(direction)) {
                    this.miniCircuit.setNode(direction, circuitNode1);
                    this.miniCircuit.setNode(direction.getOpposite(), circuitNode2);
                    break;
                }
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HyphalStemBlockEntity blockEntity) {
        if(blockEntity.miniCircuit.needsUpdate()) {
            MiniCircuitHolder.updateIfNeeded(level, pos, blockEntity.miniCircuit);
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
