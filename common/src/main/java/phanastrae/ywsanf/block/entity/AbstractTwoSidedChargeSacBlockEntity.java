package phanastrae.ywsanf.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.electromagnetism.ChargeSac;

public abstract class AbstractTwoSidedChargeSacBlockEntity extends BlockEntity {
    public static final String PRIMARY_CHARGE_SAC_KEY = "primary_charge_sac";
    public static final String SECONDARY_CHARGE_SAC_KEY = "secondary_charge_sac";

    protected final ChargeSac primaryChargeSac;
    protected final ChargeSac secondaryChargeSac;

    public AbstractTwoSidedChargeSacBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);

        this.primaryChargeSac = new ChargeSac();
        this.secondaryChargeSac = new ChargeSac();
        this.primaryChargeSac.setOnUpdate(this::sendUpdate);
        this.secondaryChargeSac.setOnUpdate(this::sendUpdate);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        if(nbt.contains(PRIMARY_CHARGE_SAC_KEY, Tag.TAG_COMPOUND)) {
            this.primaryChargeSac.loadAdditional(nbt.getCompound(PRIMARY_CHARGE_SAC_KEY), registries);
        }
        if(nbt.contains(SECONDARY_CHARGE_SAC_KEY, Tag.TAG_COMPOUND)) {
            this.secondaryChargeSac.loadAdditional(nbt.getCompound(SECONDARY_CHARGE_SAC_KEY), registries);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        CompoundTag primarySac = new CompoundTag();
        this.primaryChargeSac.saveAdditional(primarySac, registries);
        nbt.put(PRIMARY_CHARGE_SAC_KEY, primarySac);

        CompoundTag secondarySac = new CompoundTag();
        this.secondaryChargeSac.saveAdditional(secondarySac, registries);
        nbt.put(SECONDARY_CHARGE_SAC_KEY, secondarySac);
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
        long chargeDelta = ChargeSac.getChargeDeltaMilliCoulombs(blockEntity.secondaryChargeSac, blockEntity.primaryChargeSac, blockEntity.getInternalResistance());

        if(chargeDelta != 0) {
            blockEntity.pushCharge(chargeDelta);
        }
    }

    public void pushCharge(long chargeDelta) {
        ChargeSac startSac = this.secondaryChargeSac;
        ChargeSac endSac = this.primaryChargeSac;
        if(startSac != null && endSac != null) {
            startSac.addCharge(chargeDelta);
            endSac.addCharge(-chargeDelta);

            this.sendUpdate();
        }
    }

    public float getInternalResistance() {
        return 0.01F;
    }

    public ChargeSac getChargeSac() {
        return this.primaryChargeSac;
    }

    public ChargeSac getSecondaryChargeSac() {
        return this.secondaryChargeSac;
    }

    public void sendUpdate() {
        if(this.level != null && !this.level.isClientSide && this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
        }
    }
}
