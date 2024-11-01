package phanastrae.ywsanf.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import phanastrae.ywsanf.electromagnetism.CircuitNetwork;

public class VoltmeterBlockEntity extends AbstractTwoSidedChargeSacBlockEntity {
    public static final String KEY_VOLTAGE = "voltage";

    private float voltage;

    public VoltmeterBlockEntity(BlockPos pos, BlockState blockState) {
        super(YWSaNFBlockEntityTypes.VOLTMETER_BLOCK, pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        if(nbt.contains(KEY_VOLTAGE, Tag.TAG_FLOAT)) {
            this.voltage = nbt.getFloat(KEY_VOLTAGE);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        CompoundTag nbt = super.getUpdateTag(registryLookup);
        nbt.putFloat(KEY_VOLTAGE, this.voltage);
        return nbt;
    }

    public float getVoltage() {
        return this.voltage;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VoltmeterBlockEntity blockEntity) {
        CircuitNetwork network = blockEntity.wire.getStartNode().getNetwork();
        if(network != null) {
            network.tick();
        }

        float voltage = (float)blockEntity.wire.getVoltage();
        if(blockEntity.voltage != voltage) {
            blockEntity.voltage = voltage;
            blockEntity.sendUpdate();
        }
    }

    @Override
    public float getInternalResistance() {
        return 10000F;
    }
}
