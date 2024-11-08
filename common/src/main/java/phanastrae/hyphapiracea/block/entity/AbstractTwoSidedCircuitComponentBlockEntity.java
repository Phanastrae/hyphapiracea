package phanastrae.hyphapiracea.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.block.AbstractTwoSidedCircuitComponentBlock;
import phanastrae.hyphapiracea.block.MiniCircuit;
import phanastrae.hyphapiracea.block.MiniCircuitHolder;
import phanastrae.hyphapiracea.electromagnetism.CircuitNetwork;
import phanastrae.hyphapiracea.electromagnetism.CircuitNode;
import phanastrae.hyphapiracea.electromagnetism.CircuitWire;

public abstract class AbstractTwoSidedCircuitComponentBlockEntity extends BlockEntity implements MiniCircuitHolder {

    protected final MiniCircuit miniCircuit;
    protected final CircuitWire wire;

    public AbstractTwoSidedCircuitComponentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);

        this.miniCircuit = new MiniCircuit();
        CircuitNetwork network = new CircuitNetwork();
        CircuitNode positiveCircuitNode = new CircuitNode();
        positiveCircuitNode.setNetwork(network);

        CircuitNode negativeCircuitNode = new CircuitNode();
        negativeCircuitNode.setNetwork(network);

        this.wire = new CircuitWire(positiveCircuitNode, negativeCircuitNode, this.getInternalResistance(), 0);
        network.addWire(this.wire);
        this.miniCircuit.addInternalWire(this.wire);

        if(blockState.hasProperty(AbstractTwoSidedCircuitComponentBlock.FACING)) {
            Direction facing = blockState.getValue(AbstractTwoSidedCircuitComponentBlock.FACING);
            this.miniCircuit.setNode(facing, positiveCircuitNode);
            this.miniCircuit.setNode(facing.getOpposite(), negativeCircuitNode);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        CompoundTag compoundTag = this.saveCustomOnly(registryLookup);
        return compoundTag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AbstractTwoSidedCircuitComponentBlockEntity blockEntity) {
        if(blockEntity.miniCircuit.needsUpdate()) {
            MiniCircuitHolder.updateIfNeeded(level, pos, blockEntity.miniCircuit);
        }

        CircuitNetwork network = blockEntity.wire.getStartNode().getNetwork();
        CircuitNetwork network2 = blockEntity.wire.getEndNode().getNetwork();
        if(network != null) {
            network.tick(level.getGameTime());
        }
        if(network2 != null && network != network2) {
            network2.tick(level.getGameTime());
        }
    }

    public float getInternalResistance() {
        return 0.01F;
    }

    public void sendUpdate() {
        if(this.level != null && !this.level.isClientSide && this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
        }
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
    public @Nullable MiniCircuit getMiniCircuit(BlockGetter blockGetter, BlockPos pos, BlockState state, Direction side) {
        if(state.hasProperty(AbstractTwoSidedCircuitComponentBlock.FACING)) {
            Direction direction = state.getValue(AbstractTwoSidedCircuitComponentBlock.FACING);

            if(direction == side || direction == side.getOpposite()) {
                return this.miniCircuit;
            } else {
                return null;
            }
        }

        return null;
    }
}
