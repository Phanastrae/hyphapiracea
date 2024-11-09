package phanastrae.hyphapiracea.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import phanastrae.hyphapiracea.block.StormsapCellBlock;
import phanastrae.hyphapiracea.electromagnetism.CircuitNetwork;

public class StormsapCellBlockEntity extends AbstractTwoSidedCircuitComponentBlockEntity implements ClientHighlightReactingBlockEntity {
    public static final String STORED_ENERGY_KEY = "stored_energy";

    public long lastHighlightTime = -1;
    protected int lastComparatorOutput = -1;
    protected int storedEnergy;
    protected boolean active = false;

    public StormsapCellBlockEntity(BlockPos pos, BlockState blockState) {
        this(HyphaPiraceaBlockEntityTypes.STORMSAP_CELL, pos, blockState);
    }

    public StormsapCellBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
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
        AbstractTwoSidedCircuitComponentBlockEntity.serverTick(level, pos, state, blockEntity);

        double efficiency = 0.95;
        double secondsPerTick = 1 / 20.0;
        // calculate only the power dissipation caused by emf, as opposed to including the internal resistance

        double current = blockEntity.wire.getCurrent();
        int dE;
        if(current > 0) {
            // if current is flowing from positive to negative, charge
            double powerDissipation = blockEntity.wire.getPowerDissipation();
            double storedEnergyChangePerTick = powerDissipation * secondsPerTick;
            // limit efficiency of energy absorption
            storedEnergyChangePerTick *= efficiency;
            dE = Mth.floor(storedEnergyChangePerTick);
        } else {
            // if current is flowing from negative to positive, discharge
            double powerDissipation = current * blockEntity.getGeneratedVoltage();
            double storedEnergyChangePerTick = powerDissipation * secondsPerTick;
            dE = Mth.floor(storedEnergyChangePerTick);
        }

        if (dE != 0) {
            blockEntity.addEnergy(dE);
            blockEntity.lastComparatorOutput = blockEntity.calculateComparatorOutput();
            blockEntity.setChanged();
            blockEntity.sendUpdate();
        }

        CircuitNetwork network = blockEntity.wire.getStartNode().getNetwork();
        if(!blockEntity.active && blockEntity.storedEnergy > 0) {
            blockEntity.active = true;
            // wire flows from positive start to negative end, emf here refers to push from start to end, we want push from negative (end) to positive (start) hence negative emf
            blockEntity.wire.setEmf(-blockEntity.getGeneratedVoltage());
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

        if(blockEntity.lastComparatorOutput == -1) {
            blockEntity.lastComparatorOutput = blockEntity.calculateComparatorOutput();
            blockEntity.setChanged();
        }
    }

    public void sendUpdate() {
        if(this.level != null && !this.level.isClientSide && this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
        }
    }

    public int calculateComparatorOutput() {
        return Mth.clamp(Mth.ceil(this.storedEnergy / (float)this.maxEnergyStorage() * 15), 0, 15);
    }

    public int getComparatorOutput() {
        if(this.lastComparatorOutput == -1) {
            this.lastComparatorOutput = this.calculateComparatorOutput();
            this.setChanged();
        }

        return this.lastComparatorOutput;
    }

    @Override
    public float getInternalResistance() {
        return 0.01F;
    }

    public float getGeneratedVoltage() {
        return 12F;
    }

    public int maxEnergyStorage() {
        return 1000000;
    }

    public int maxEnergyDeficit() {
        return -10000;
    }

    public int getStoredEnergy() {
        return this.storedEnergy;
    }

    public int getPositiveStoredEnergy() {
        if(this.storedEnergy < 0) {
            return 0;
        } else {
            return this.storedEnergy;
        }
    }

    public void addEnergy(int energy) {
        this.storedEnergy += energy;
        if(this.storedEnergy < this.maxEnergyDeficit()) {
            this.storedEnergy = this.maxEnergyDeficit();
        }
        if(this.storedEnergy > this.maxEnergyStorage()) {
            this.storedEnergy = this.maxEnergyStorage();
        }
        this.setChanged();

        double v = this.storedEnergy / 1000000F;
        int energyLevel = Mth.clamp(Mth.ceil(v * 15), 0, 15);

        if(this.getBlockState().hasProperty(StormsapCellBlock.STORED_POWER)) {
            BlockState newState = this.getBlockState().setValue(StormsapCellBlock.STORED_POWER, energyLevel);
            if (this.level != null && !this.level.isClientSide && this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
                this.level.setBlock(this.getBlockPos(), newState, 3);
            }
        }
    }

    @Override
    public void onHighlight() {
        if(this.getLevel() != null) {
            this.lastHighlightTime = this.getLevel().getGameTime();
        }
    }
}
