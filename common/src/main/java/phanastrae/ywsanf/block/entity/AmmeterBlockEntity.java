package phanastrae.ywsanf.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import phanastrae.ywsanf.electromagnetism.ChargeSac;

public class AmmeterBlockEntity extends AbstractTwoSidedChargeSacBlockEntity {

    private float current;

    public AmmeterBlockEntity(BlockPos pos, BlockState blockState) {
        super(YWSaNFBlockEntityTypes.AMMETER_BLOCK, pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        if(nbt.contains("current", Tag.TAG_FLOAT)) {
            this.current = nbt.getFloat("current");
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        CompoundTag nbt = super.getUpdateTag(registryLookup);
        nbt.putFloat("current", this.current);
        return nbt;
    }

    public float getCurrent() {
        return this.current;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AmmeterBlockEntity blockEntity) {
        long chargeDelta = ChargeSac.getChargeDeltaMilliCoulombs(blockEntity.secondaryChargeSac, blockEntity.primaryChargeSac, blockEntity.getInternalResistance());

        if(chargeDelta != 0) {
            blockEntity.pushCharge(chargeDelta);
        }

        float current = -chargeDelta * 20 / 1000F;
        if(current != blockEntity.current) {
            blockEntity.current = current;
            blockEntity.sendUpdate();
        }
    }
}
