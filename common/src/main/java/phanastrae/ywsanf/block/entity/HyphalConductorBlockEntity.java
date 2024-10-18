package phanastrae.ywsanf.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.block.HyphalConductorBlock;
import phanastrae.ywsanf.block.state.ConductorStateProperty;
import phanastrae.ywsanf.entity.YWSaNFEntityAttachment;

import java.util.List;
import java.util.UUID;

public class HyphalConductorBlockEntity extends BlockEntity implements Clearable, ContainerSingleItem.BlockContainerSingleItem {
    public static final String TAG_WIRE_ITEM = "wire_item";
    public static final String TAG_LINKED_ENTITY = "linked_entity";
    public static final String TAG_LINKED_BLOCK_RELATIVE_X = "linked_block_relative_x";
    public static final String TAG_LINKED_BLOCK_RELATIVE_Y = "linked_block_relative_y";
    public static final String TAG_LINKED_BLOCK_RELATIVE_Z = "linked_block_relative_z";
    public static final double MAX_HELD_WIRE_RANGE = 16.0;

    private ItemStack item = ItemStack.EMPTY;
    @Nullable
    private Entity linkedEntity;
    @Nullable
    private BlockPos linkedBlockPos;

    private boolean hasObservedEndpoint = true;
    private int checkEndpointTimer = 0;

    public HyphalConductorBlockEntity(BlockPos pos, BlockState blockState) {
        super(YWSaNFBlockEntityTypes.HYPHAL_CONDUCTOR, pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        if (nbt.contains(TAG_WIRE_ITEM, 10)) {
            this.item = ItemStack.parse(registries, nbt.getCompound(TAG_WIRE_ITEM)).orElse(ItemStack.EMPTY);
        } else {
            this.item = ItemStack.EMPTY;
        }
        if(nbt.hasUUID(TAG_LINKED_ENTITY)) {
            UUID uuid = nbt.getUUID(TAG_LINKED_ENTITY);

            if(this.level instanceof ServerLevel serverLevel && !this.level.isClientSide()) {
                Entity e = serverLevel.getEntity(uuid);
                if(e != null && canLinkTo(e)) {
                    this.linkTo(e);
                } else {
                    this.unlink();
                }
            } else if(this.level != null && this.level.isClientSide()) {
                // note: if the entity is not yet loaded on the client when the block entity is loaded, they won't be linked
                // this probably isn't ideal, but given the context (players temporarily holding wires) it shouldn't really matter
                double scanRange = MAX_HELD_WIRE_RANGE * 1.25F;
                List<Entity> e = this.level.getEntities((Entity)null, AABB.ofSize(this.getBlockPos().getCenter(), scanRange, scanRange, scanRange), (entity -> entity.getUUID().equals(uuid)));
                if(e.isEmpty()) {
                    this.unlink();
                } else {
                    this.linkTo(e.getFirst());
                }
            }
        } else {
            if(this.linkedEntity != null) {
                this.unlink();
            }
        }
        if(nbt.contains(TAG_LINKED_BLOCK_RELATIVE_X, Tag.TAG_INT) && nbt.contains(TAG_LINKED_BLOCK_RELATIVE_Y, Tag.TAG_INT) && nbt.contains(TAG_LINKED_BLOCK_RELATIVE_Z, Tag.TAG_INT)) {
            int x = nbt.getInt(TAG_LINKED_BLOCK_RELATIVE_X);
            int y = nbt.getInt(TAG_LINKED_BLOCK_RELATIVE_Y);
            int z = nbt.getInt(TAG_LINKED_BLOCK_RELATIVE_Z);
            BlockPos pos = this.getBlockPos().offset(x, y, z);
            this.linkTo((pos));

            this.hasObservedEndpoint = false;
            this.checkEndpointTimer = 0;
        } else {
            if(this.linkedBlockPos != null) {
                this.unlink();

                this.hasObservedEndpoint = true;
                this.checkEndpointTimer = 0;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        if (!this.getTheItem().isEmpty()) {
            nbt.put(TAG_WIRE_ITEM, this.getTheItem().save(registries));
        }
        if(this.linkedEntity != null) {
            nbt.putUUID(TAG_LINKED_ENTITY, this.linkedEntity.getUUID());
        }
        if(this.linkedBlockPos != null) {
            Vec3i relPos = this.linkedBlockPos.subtract(this.getBlockPos());
            nbt.putInt(TAG_LINKED_BLOCK_RELATIVE_X, relPos.getX());
            nbt.putInt(TAG_LINKED_BLOCK_RELATIVE_Y, relPos.getY());
            nbt.putInt(TAG_LINKED_BLOCK_RELATIVE_Z, relPos.getZ());
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        CompoundTag nbtCompound = this.saveCustomOnly(registryLookup);
        return nbtCompound;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HyphalConductorBlockEntity blockEntity) {
        Entity linkedEntity = blockEntity.linkedEntity;
        if(linkedEntity != null) {
            if(!blockEntity.canLinkTo(linkedEntity)) {
                blockEntity.unlink();
                blockEntity.sendUpdate();

                level.playSound(null, linkedEntity.getX(), linkedEntity.getEyeY(), linkedEntity.getZ(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }

        if(!blockEntity.hasObservedEndpoint) {
            if (blockEntity.checkEndpointTimer <= 0) {
                if (blockEntity.observeEndpoint()) {
                    blockEntity.hasObservedEndpoint = true;
                } else {
                    blockEntity.checkEndpointTimer = 19;
                }
            } else {
                blockEntity.checkEndpointTimer--;
            }
        }
    }

    @Override
    public void setRemoved() {
        if(this.linkedEntity != null) {
            YWSaNFEntityAttachment.getAttachment(this.linkedEntity).unlinkTo(this);
        }
        super.setRemoved();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if(this.linkedEntity != null) {
            YWSaNFEntityAttachment.getAttachment(this.linkedEntity).linkTo(this);
        }
        this.hasObservedEndpoint = false;
        this.checkEndpointTimer = 0;
    }

    public boolean observeEndpoint() {
        // check if the endpoint is valid, and if not then disconnect
        if(this.linkedBlockPos == null) {
            return true;
        }
        if(this.level == null) {
            return false;
        }
        if(!this.level.isLoaded(this.linkedBlockPos)) {
            return false;
        }

        BlockEntity linkedBE = this.level.getBlockEntity(this.linkedBlockPos);
        if(linkedBE instanceof HyphalConductorBlockEntity linkedBlockEntity) {
            if(this.getBlockPos().equals(linkedBlockEntity.getLinkedBlockPos())) {
                if(this.hasItem() != linkedBlockEntity.hasItem()) {
                    return true;
                }
            }
        }

        this.unlink();
        this.checkBlockStateAndSendUpdate();
        return true;
    }

    public boolean canLinkTo(Entity entity) {
        if(entity.isRemoved() || !entity.isAlive() || entity.isSpectator()) {
            return false;
        }
        return canLinkTo(entity.position());
    }

    public boolean canLinkTo(HyphalConductorBlockEntity blockEntity) {
        return canLinkTo(blockEntity.getBlockPos());
    }

    public boolean canLinkTo(BlockPos pos) {
        return canLinkTo(pos.getCenter());
    }

    public boolean canLinkTo(Vec3 pos) {
        return this.getBlockPos().getCenter().distanceToSqr(pos) <= MAX_HELD_WIRE_RANGE * MAX_HELD_WIRE_RANGE;
    }

    public void unlink() {
        if(this.linkedEntity != null) {
            Entity e = this.linkedEntity;
            this.linkedEntity = null;
            if(!this.isRemoved()) {
                YWSaNFEntityAttachment.getAttachment(e).unlinkTo(this);
            }
        }
        if(this.linkedBlockPos != null) {
            BlockPos bp = this.linkedBlockPos;
            this.linkedBlockPos = null;
            if(!this.isRemoved() && this.level != null && !this.level.isClientSide) {
                if(this.level.getBlockEntity(bp) instanceof HyphalConductorBlockEntity linkedBlockEntity) {
                    linkedBlockEntity.unlink();
                    linkedBlockEntity.checkBlockStateAndSendUpdate();
                }
            }
        }
    }

    public void linkTo(@Nullable Entity entity) {
        if(entity == this.linkedEntity) {
            return;
        }

        unlink();
        this.linkedEntity = entity;

        if(!this.isRemoved() && this.linkedEntity != null) {
            YWSaNFEntityAttachment.getAttachment(this.linkedEntity).linkTo(this);
        }

        this.sendUpdate();
    }

    public void linkTo(@Nullable BlockPos blockPos) {
        if(blockPos == null && this.linkedBlockPos == null) {
            return;
        }
        if(blockPos != null && blockPos.equals(this.linkedBlockPos)) {
            return;
        }

        unlink();
        this.linkedBlockPos = blockPos;

        if(!this.isRemoved() && this.linkedBlockPos != null) {
            if(this.level != null && !this.level.isClientSide && this.level.getBlockEntity(this.linkedBlockPos) instanceof HyphalConductorBlockEntity linkedBlockEntity) {
                linkedBlockEntity.linkedBlockPos = this.getBlockPos();
                linkedBlockEntity.checkBlockStateAndSendUpdate();
            }
        }

        checkBlockStateAndSendUpdate();
    }

    public void checkBlockStateAndSendUpdate() {
        if(!updateBlockStateIfNeeded()) {
            sendUpdate();
        }
    }

    public boolean updateBlockStateIfNeeded() {
        BlockState oldState = this.getBlockState();
        BlockState newState = this.getBlockState();
        if(oldState.hasProperty(HyphalConductorBlock.CONDUCTOR_STATE)) {
            ConductorStateProperty.ConductorState conductorState;
            if(this.hasItem()) {
                conductorState = ConductorStateProperty.ConductorState.HOLDING_WIRE;
            } else if(this.linkedBlockPos != null) {
                conductorState = ConductorStateProperty.ConductorState.ACCEPTING_WIRE;
            } else {
                conductorState = ConductorStateProperty.ConductorState.EMPTY;
            }
            newState = newState.setValue(HyphalConductorBlock.CONDUCTOR_STATE, conductorState);
        }

        if(!oldState.equals(newState)) {
            if(this.level != null && !this.level.isClientSide && this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
                this.level.setBlock(this.getBlockPos(), newState, 3);
                level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(this.getBlockState()));
            }
            return true;
        } else {
            return false;
        }
    }

    public void sendUpdate() {
        if(this.level != null && !this.level.isClientSide && this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
        }
    }

    @Nullable
    public Entity getLinkedEntity() {
        return this.linkedEntity;
    }

    @Nullable
    public BlockPos getLinkedBlockPos() {
        return this.linkedBlockPos;
    }

    public void popOutTheItem() {
        if (this.level != null && !this.level.isClientSide) {
            BlockPos blockpos = this.getBlockPos();
            ItemStack itemstack = this.getTheItem();
            if (!itemstack.isEmpty()) {
                this.removeTheItem();

                Vec3 vec3 = Vec3.atLowerCornerWithOffset(blockpos, 0.5, 1.01, 0.5).offsetRandom(this.level.random, 0.7F);
                ItemStack itemstack1 = itemstack.copy();
                ItemEntity itementity = new ItemEntity(this.level, vec3.x(), vec3.y(), vec3.z(), itemstack1);
                itementity.setDefaultPickUpDelay();
                this.level.addFreshEntity(itementity);
            }
        }
    }

    public boolean hasItem() {
        return !this.item.isEmpty();
    }

    @Override
    public ItemStack getTheItem() {
        return this.item;
    }

    @Override
    public ItemStack splitTheItem(int amount) {
        ItemStack itemstack = this.item;
        this.setTheItem(ItemStack.EMPTY);
        return itemstack;
    }

    @Override
    public void setTheItem(ItemStack item) {
        this.item = item;
    }

    @Override
    public BlockEntity getContainerBlockEntity() {
        return this;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return false;
    }
}
