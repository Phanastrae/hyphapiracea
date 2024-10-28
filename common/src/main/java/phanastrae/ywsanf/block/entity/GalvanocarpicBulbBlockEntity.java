package phanastrae.ywsanf.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.electromagnetism.ChargeSac;

public class GalvanocarpicBulbBlockEntity extends BlockEntity {
    public static final String CHARGE_SAC_KEY = "charge_sac";

    private final ChargeSac chargeSac;

    public GalvanocarpicBulbBlockEntity(BlockPos pos, BlockState blockState) {
        super(YWSaNFBlockEntityTypes.GALVANOCARPIC_BULB, pos, blockState);

        this.chargeSac = new ChargeSac();
        this.chargeSac.setOnUpdate(this::sendUpdate);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        if(nbt.contains(CHARGE_SAC_KEY, Tag.TAG_COMPOUND)) {
            this.chargeSac.loadAdditional(nbt.getCompound(CHARGE_SAC_KEY), registries);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        CompoundTag primarySac = new CompoundTag();
        this.chargeSac.saveAdditional(primarySac, registries);
        nbt.put(CHARGE_SAC_KEY, primarySac);
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

    public ChargeSac getChargeSac() {
        return this.chargeSac;
    }

    public void sendUpdate() {
        if(this.level != null && !this.level.isClientSide && this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
        }
    }
}
