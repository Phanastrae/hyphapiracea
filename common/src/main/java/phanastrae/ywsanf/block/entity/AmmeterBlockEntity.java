package phanastrae.ywsanf.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import phanastrae.ywsanf.electromagnetism.CircuitNetwork;

public class AmmeterBlockEntity extends AbstractTwoSidedChargeSacBlockEntity {
    public static final String KEY_CURRENT = "current";

    private float current;

    public AmmeterBlockEntity(BlockPos pos, BlockState blockState) {
        super(YWSaNFBlockEntityTypes.AMMETER_BLOCK, pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        if(nbt.contains(KEY_CURRENT, Tag.TAG_FLOAT)) {
            this.current = nbt.getFloat(KEY_CURRENT);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        CompoundTag nbt = super.getUpdateTag(registryLookup);
        nbt.putFloat(KEY_CURRENT, this.current);
        return nbt;
    }

    public float getCurrent() {
        return this.current;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AmmeterBlockEntity blockEntity) {
        CircuitNetwork network = blockEntity.wire.getStartNode().getNetwork();
        if(network != null) {
            network.tick();
        }

        float current = (float)blockEntity.wire.getCurrent();
        if(blockEntity.current != current) {
            blockEntity.current = current;
            blockEntity.sendUpdate();
        }
    }
}
