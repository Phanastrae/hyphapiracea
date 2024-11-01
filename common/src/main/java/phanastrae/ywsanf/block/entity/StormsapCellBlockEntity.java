package phanastrae.ywsanf.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import phanastrae.ywsanf.electromagnetism.CircuitNetwork;

public class StormsapCellBlockEntity extends AbstractTwoSidedChargeSacBlockEntity {
    public static final String STORED_ENERGY_KEY = "stored_energy";

    private int storedEnergy;
    private boolean active = false;

    public StormsapCellBlockEntity(BlockPos pos, BlockState blockState) {
        super(YWSaNFBlockEntityTypes.STORMSAP_CELL, pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        if(nbt.contains(STORED_ENERGY_KEY, CompoundTag.TAG_INT)) {
            this.storedEnergy = nbt.getInt(STORED_ENERGY_KEY);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putInt(STORED_ENERGY_KEY, this.storedEnergy);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StormsapCellBlockEntity blockEntity) {
        if(blockEntity.active) {
            double current = blockEntity.wire.getCurrent();
            double pd = blockEntity.getGeneratedVoltage();
            double power = current * pd;
            int dE = Mth.ceil(power / 20);
            if(dE != 0) {
                blockEntity.storedEnergy -= dE;
                blockEntity.setChanged();
                blockEntity.sendUpdate();
            }
        }

        CircuitNetwork network = blockEntity.wire.getStartNode().getNetwork();
        if(!blockEntity.active && blockEntity.storedEnergy > 0) {
            blockEntity.active = true;
            blockEntity.wire.setEmf(blockEntity.getGeneratedVoltage());
            if(network != null) {
                network.markNeedsUpdate();
            }
        } else if(blockEntity.active && blockEntity.storedEnergy <= 0) {
            blockEntity.active = false;
            blockEntity.wire.setEmf(0);
            if(network != null) {
                network.markNeedsUpdate();
            }
        }

        if(network != null) {
            network.tick();
        }
    }

    public void sendUpdate() {
        if(this.level != null && !this.level.isClientSide && this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
        }
    }

    @Override
    public float getInternalResistance() {
        return 0.01F;
    }

    public float getGeneratedVoltage() {
        return 12F;
    }

    public int getStoredEnergy() {
        return this.storedEnergy;
    }

    public void addEnergy(int energy) {
        this.storedEnergy += energy;
        this.setChanged();
    }
}
