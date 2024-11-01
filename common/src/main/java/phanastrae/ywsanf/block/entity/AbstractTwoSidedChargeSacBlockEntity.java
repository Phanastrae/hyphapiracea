package phanastrae.ywsanf.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.electromagnetism.CircuitNetwork;
import phanastrae.ywsanf.electromagnetism.CircuitNode;
import phanastrae.ywsanf.electromagnetism.CircuitWire;

public abstract class AbstractTwoSidedChargeSacBlockEntity extends BlockEntity {

    protected final CircuitNode positiveCircuitNode;
    protected final CircuitNode negativeCircuitNode;
    protected final CircuitWire wire;

    public AbstractTwoSidedChargeSacBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);

        this.positiveCircuitNode = new CircuitNode();
        this.negativeCircuitNode = new CircuitNode();

        CircuitNetwork network = new CircuitNetwork();
        this.positiveCircuitNode.setNetwork(network);
        this.negativeCircuitNode.setNetwork(network);
        this.wire = new CircuitWire(this.positiveCircuitNode, this.negativeCircuitNode, this.getInternalResistance(), 0);
        network.addWire(this.wire);
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

    public static void serverTick(Level level, BlockPos pos, BlockState state, AbstractTwoSidedChargeSacBlockEntity blockEntity) {
        CircuitNetwork network = blockEntity.wire.getStartNode().getNetwork();
        if(network != null) {
            network.tick();
        }
    }

    public float getInternalResistance() {
        return 0.01F;
    }

    public CircuitNode getPositiveCircuitNode() {
        return this.positiveCircuitNode;
    }

    public CircuitNode getNegativeCircuitNode() {
        return this.negativeCircuitNode;
    }

    public void sendUpdate() {
        if(this.level != null && !this.level.isClientSide && this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
        }
    }

    @Override
    public void setRemoved() {
        if(this.wire.getStartNode().getNetwork() != null) {
            this.wire.getStartNode().getNetwork().removeWire(this.wire);
        }
        super.setRemoved();
    }
}
