package phanastrae.hyphapiracea.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class VoltmeterBlockEntity extends AbstractTwoSidedChargeSacBlockEntity implements ClientHighlightReactingBlockEntity {
    public static final String KEY_VOLTAGE = "voltage";

    public long lastHighlightTime = -1;
    private int lastComparatorOutput = -1;
    private double voltage;

    public VoltmeterBlockEntity(BlockPos pos, BlockState blockState) {
        super(HyphaPiraceaBlockEntityTypes.HYPHAL_VOLTMETER, pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        if(nbt.contains(KEY_VOLTAGE, Tag.TAG_DOUBLE)) {
            this.voltage = nbt.getDouble(KEY_VOLTAGE);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        CompoundTag nbt = super.getUpdateTag(registryLookup);
        nbt.putDouble(KEY_VOLTAGE, this.voltage);
        return nbt;
    }

    public double getVoltage() {
        return this.voltage;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VoltmeterBlockEntity blockEntity) {
        AbstractTwoSidedChargeSacBlockEntity.serverTick(level, pos, state, blockEntity);

        double voltage = blockEntity.wire.getVoltage();
        if(blockEntity.voltage != voltage || blockEntity.lastComparatorOutput == -1) {
            blockEntity.voltage = voltage;
            blockEntity.lastComparatorOutput = blockEntity.calculateComparatorOutput();
            blockEntity.sendUpdate();
            blockEntity.setChanged();
        }
    }

    public int calculateComparatorOutput() {
        int comparatorOutput = 0;
        double strengthToBeat = 0.25;
        double voltage = Math.abs(this.voltage);
        for(int i = 0; i < 15; i++) {
            if(voltage >= strengthToBeat) {
                strengthToBeat *= 2;
                comparatorOutput += 1;
            } else {
                break;
            }
        }

        return comparatorOutput;
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
        return 10000F;
    }

    @Override
    public void onHighlight() {
        if(this.getLevel() != null) {
            this.lastHighlightTime = this.getLevel().getGameTime();
        }
    }
}
