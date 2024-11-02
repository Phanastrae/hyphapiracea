package phanastrae.hyphapiracea.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import phanastrae.hyphapiracea.block.HyphalConductorBlock;
import phanastrae.hyphapiracea.block.MiniCircuit;
import phanastrae.hyphapiracea.block.MiniCircuitHolder;
import phanastrae.hyphapiracea.block.state.ConductorStateProperty;
import phanastrae.hyphapiracea.component.HyphaPiraceaComponentTypes;
import phanastrae.hyphapiracea.component.type.WireLineComponent;
import phanastrae.hyphapiracea.electromagnetism.CircuitNetwork;
import phanastrae.hyphapiracea.electromagnetism.CircuitNode;
import phanastrae.hyphapiracea.electromagnetism.CircuitWire;
import phanastrae.hyphapiracea.electromagnetism.WireLine;
import phanastrae.hyphapiracea.entity.HyphaPiraceaEntityAttachment;
import phanastrae.hyphapiracea.particle.HyphaPiraceaParticleTypes;
import phanastrae.hyphapiracea.world.HyphaPiraceaLevelAttachment;

import java.util.List;
import java.util.UUID;

public class HyphalConductorBlockEntity extends BlockEntity implements Clearable, ContainerSingleItem.BlockContainerSingleItem {
    public static final String TAG_WIRE_ITEM = "wire_item";
    public static final String TAG_LINKED_ENTITY = "linked_entity";
    public static final String TAG_LINKED_BLOCK_RELATIVE_X = "linked_block_relative_x";
    public static final String TAG_LINKED_BLOCK_RELATIVE_Y = "linked_block_relative_y";
    public static final String TAG_LINKED_BLOCK_RELATIVE_Z = "linked_block_relative_z";

    private final WireLine wireLine;
    private boolean inLevelList = false;

    private ItemStack item = ItemStack.EMPTY;
    @Nullable
    private Entity linkedEntity;
    @Nullable
    private BlockPos linkedBlockPos;

    private boolean hasObservedEndpoint = true;
    private int checkEndpointTimer = 0;

    public HyphalConductorBlockEntity(BlockPos pos, BlockState blockState) {
        super(HyphaPiraceaBlockEntityTypes.HYPHAL_CONDUCTOR, pos, blockState);

        this.wireLine = new WireLine(pos.getCenter());
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        // note: item loading needs to happen early to get access to the maxWireRange
        if (nbt.contains(TAG_WIRE_ITEM, 10)) {
            this.setTheItem(ItemStack.parse(registries, nbt.getCompound(TAG_WIRE_ITEM)).orElse(ItemStack.EMPTY));
        } else {
            this.setTheItem(ItemStack.EMPTY);
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
                // note: item needs to have been loaded before this point
                double scanRange = this.getMaxWireRange() * 2.25F;

                // note: if the entity is not yet loaded on the client when the block entity is loaded, they won't be linked
                // this probably isn't ideal, but given the context (players temporarily holding wires) it shouldn't really matter
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
            this.linkTo(pos);

            this.hasObservedEndpoint = false;
            this.checkEndpointTimer = 0;
        } else {
            if(this.linkedBlockPos != null) {
                this.unlink();

                this.hasObservedEndpoint = true;
                this.checkEndpointTimer = 0;
            }
        }

        if(nbt.contains("current", Tag.TAG_FLOAT)) {
            this.setCurrent(nbt.getFloat("current"));
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
        CompoundTag compoundTag = this.saveCustomOnly(registryLookup);
        compoundTag.putFloat("current", this.wireLine.getCurrent());

        // send non-empty tag to avoid issues
        compoundTag.putInt("dummy", 0);

        return compoundTag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Nullable
    private CircuitWire wire;

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

        boolean wireValid = false;
        if(blockEntity.linkedBlockPos != null && blockEntity.hasItem()) {
            CircuitNode startNode = getSupportingNode(level, pos);
            CircuitNode endNode = getSupportingNode(level, blockEntity.linkedBlockPos);
            if(startNode != null && endNode != null && (blockEntity.wire == null || (blockEntity.wire.getStartNode() == startNode && blockEntity.wire.getEndNode() == endNode))) {
                wireValid = true;

                if(blockEntity.wire == null) {
                    for(Direction direction : Direction.values()) {
                        BlockPos neighborPos1 = pos.offset(direction.getNormal());
                        BlockPos neighborPos2 = blockEntity.linkedBlockPos.offset(direction.getNormal());
                        MiniCircuitHolder.updateIfNeeded(level, neighborPos1, direction.getOpposite());
                        MiniCircuitHolder.updateIfNeeded(level, neighborPos2, direction.getOpposite());
                    }

                    if(startNode.getNetwork() == null) {
                        startNode.setNetwork(new CircuitNetwork());
                    }
                    if(endNode.getNetwork() == null) {
                        endNode.setNetwork(new CircuitNetwork());
                    }
                    if(endNode.getNetwork() != startNode.getNetwork()) {
                        CircuitNetwork network = endNode.getNetwork();
                        network.merge(startNode.getNetwork());
                        startNode.setNetwork(network);
                    }

                    blockEntity.wire = new CircuitWire(startNode, endNode, blockEntity.wireLine.getTotalResistance(), 0);
                    startNode.getNetwork().addWire(blockEntity.wire);
                }
            }
        }
        if(!wireValid && blockEntity.wire != null) {
            CircuitNetwork network = blockEntity.wire.getStartNode().getNetwork();
            CircuitNetwork network2 = blockEntity.wire.getEndNode().getNetwork();
            if(network != null) {
                network.removeWire(blockEntity.wire);
            }
            if(network != network2 && network2 != null) {
                network2.removeWire(blockEntity.wire);
            }
            blockEntity.wire = null;
        }

        if(blockEntity.wire != null) {
            CircuitNetwork network = blockEntity.wire.getStartNode().getNetwork();
            if(network != null) {
                network.tick(level.getGameTime());
            }

            float current = (float)(blockEntity.wire.getCurrent());
            if(current != blockEntity.wireLine.getCurrent()) {
                blockEntity.setCurrent(current);
                blockEntity.sendUpdate();
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, HyphalConductorBlockEntity blockEntity) {
        if(blockEntity.hasItem() && blockEntity.linkedBlockPos != null) {
            WireLineComponent wlc = blockEntity.getWireLineComponent();

            if(wlc != null) {
                RandomSource random = level.random;
                Vec3 start = pos.getCenter();
                Vec3 end = blockEntity.linkedBlockPos.getCenter();

                double length = end.subtract(start).length();
                double rpb = wlc.resistancePerBlock();
                double resistance = rpb * length;
                double current = blockEntity.wireLine.getCurrent();
                double power = current * current * resistance;

                int particleCount = Math.clamp(Mth.ceil(Math.sqrt(power * 0.1) * random.nextFloat() * random.nextFloat() - 0.01), 0, 2);

                double dx = end.x - start.x;
                double dy = end.y - start.y;
                double dz = end.z - start.z;
                for (int i = 0; i < particleCount; i++) {
                    double t = random.nextFloat();
                    double y = start.y + (dy > 0 ? dy * t * t : dy - dy * (1.0 - t) * (1.0 - t));

                    double x = start.x + t * dx;
                    double z = start.z + t * dz;

                    double s = 0.001 + Math.min(Math.sqrt(power * 0.01) * random.nextFloat(), 3.0);
                    s = (random.nextFloat() * 2.0 - 1.0) * s;
                    float f = random.nextFloat();
                    if(f > 0.3) {
                        level.addParticle(HyphaPiraceaParticleTypes.ZAPPY_GRIT, x, y, z, s, s, s);
                    } else if(f > 0.1) {
                        level.addParticle(ParticleTypes.FALLING_NECTAR, x, y, z, s * 0.3, -0.1, s * 0.3);
                    } else {
                        level.addParticle(HyphaPiraceaParticleTypes.FAIRY_FOG, x, y, z, s * 0.3, s * 0.3, s * 0.3);
                    }
                }
            }
        }
    }

    @Nullable
    public static CircuitNode getSupportingNode(Level level, BlockPos coilPos) {
        BlockState state = level.getBlockState(coilPos);
        if(state.hasProperty(HyphalConductorBlock.FACING)) {
            Direction facing = state.getValue(HyphalConductorBlock.FACING);
            BlockPos supportPos = coilPos.offset(facing.getOpposite().getNormal());
            BlockState supportState = level.getBlockState(supportPos);
            if(supportState.getBlock() instanceof MiniCircuitHolder mch) {
                MiniCircuit miniCircuit = mch.getMiniCircuit(level, supportPos, supportState, facing);
                if(miniCircuit != null) {
                    return miniCircuit.getNode(facing);
                } else {
                    return null;
                }
            }
        }

        return null;
    }

    @Override
    public void setRemoved() {
        if(this.wire != null && this.wire.getStartNode().getNetwork() != null) {
            this.wire.getStartNode().getNetwork().removeWire(this.wire);
        }
        if(this.linkedEntity != null) {
            HyphaPiraceaEntityAttachment.getAttachment(this.linkedEntity).unlinkTo(this);
        }
        this.removeFromLevelListIfPossible();
        super.setRemoved();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        if(this.linkedEntity != null) {
            HyphaPiraceaEntityAttachment.getAttachment(this.linkedEntity).linkTo(this);
        }
        updateLevelListStatus(false);
        this.hasObservedEndpoint = false;
        this.checkEndpointTimer = 0;
    }

    @Override
    public void setLevel(Level level) {
        if(this.level != level) {
            this.removeFromLevelListIfPossible();
        }
        super.setLevel(level);
        updateLevelListStatus(false);
    }

    private void updateLevelListStatus(boolean needsAreaUpdate) {
        boolean canAffectWorld = !this.isRemoved() && this.wireLine.getMaxPossibleInfluenceRadiusSqr() > 0 && this.linkedBlockPos != null;

        if(canAffectWorld) {
            if(!this.inLevelList) {
                addToLevelListIfPossible();
            } else if(needsAreaUpdate) {
                this.updateInLevelListIfPossible();
            }
        } else {
            removeFromLevelListIfPossible();
        }
    }

    private void addToLevelListIfPossible() {
        if(!this.inLevelList && this.level != null) {
            HyphaPiraceaLevelAttachment.getAttachment(this.level).addWire(this.wireLine);
            this.inLevelList = true;
        }
    }

    private void removeFromLevelListIfPossible() {
        if(this.inLevelList && this.level != null) {
            HyphaPiraceaLevelAttachment.getAttachment(this.level).removeWire(this.wireLine);
            this.inLevelList = false;
        }
    }

    private void updateInLevelListIfPossible() {
        if(this.inLevelList && this.level != null) {
            HyphaPiraceaLevelAttachment.getAttachment(this.level).updateWire(this.wireLine);
        }
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
        return canLinkTo(entity.getEyePosition());
    }

    public boolean canLinkTo(HyphalConductorBlockEntity blockEntity) {
        return canLinkTo(blockEntity.getBlockPos());
    }

    public boolean canLinkTo(BlockPos pos) {
        return canLinkTo(pos.getCenter());
    }

    public boolean canLinkTo(Vec3 pos) {
        float range = this.getMaxWireRange();
        return this.getBlockPos().getCenter().distanceToSqr(pos) <= range * range;
    }

    public void unlink() {
        if(this.linkedEntity != null) {
            Entity e = this.linkedEntity;
            this.linkedEntity = null;
            if(!this.isRemoved()) {
                HyphaPiraceaEntityAttachment.getAttachment(e).unlinkTo(this);
            }
        }
        if(this.linkedBlockPos != null) {
            BlockPos bp = this.linkedBlockPos;
            this.setLinkedBlockPos(null);
            if(!this.isRemoved() && this.level != null && !this.level.isClientSide) {
                if(this.level.getBlockEntity(bp) instanceof HyphalConductorBlockEntity linkedBlockEntity) {
                    linkedBlockEntity.unlink();
                    linkedBlockEntity.checkBlockStateAndSendUpdate();
                }
            }
        }

        this.setChanged();
    }

    public void linkTo(@Nullable Entity entity) {
        if(entity == this.linkedEntity) {
            return;
        }

        unlink();
        this.linkedEntity = entity;

        if(!this.isRemoved() && this.linkedEntity != null) {
            HyphaPiraceaEntityAttachment.getAttachment(this.linkedEntity).linkTo(this);
        }

        this.setChanged();
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
        this.setLinkedBlockPos(blockPos);

        if(!this.isRemoved() && this.linkedBlockPos != null) {
            if(this.level != null && !this.level.isClientSide && this.level.getBlockEntity(this.linkedBlockPos) instanceof HyphalConductorBlockEntity linkedBlockEntity) {
                linkedBlockEntity.setLinkedBlockPos(this.getBlockPos());
                linkedBlockEntity.checkBlockStateAndSendUpdate();
            }
        }

        checkBlockStateAndSendUpdate();
    }

    private void setLinkedBlockPos(@Nullable BlockPos blockPos) {
        this.linkedBlockPos = blockPos;
        if(blockPos == null) {
            this.wireLine.setEndPoint(this.wireLine.getStart());
        } else {
            this.wireLine.setEndPoint(blockPos.getCenter());
        }
        this.setChanged();
        this.updateLevelListStatus(false);
    }

    public void setCurrent(float current) {
        this.wireLine.setCurrent(current);
        if(this.inLevelList && this.level != null) {
            HyphaPiraceaLevelAttachment.getAttachment(this.level).updateWire(this.wireLine);
        }
    }

    public void setDropoffRadius(float dropoffRadius) {
        this.wireLine.setDropoffRadius(dropoffRadius);
        if(this.inLevelList && this.level != null) {
            HyphaPiraceaLevelAttachment.getAttachment(this.level).updateWire(this.wireLine);
        }
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

    public WireLine getWireLine() {
        return this.wireLine;
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
        this.setChanged();
    }

    public boolean hasItem() {
        return !this.item.isEmpty();
    }

    @Nullable
    public WireLineComponent getWireLineComponent() {
        if(!this.item.isEmpty() && this.item.has(HyphaPiraceaComponentTypes.WIRE_LINE_COMPONENT)) {
            return this.item.get(HyphaPiraceaComponentTypes.WIRE_LINE_COMPONENT);
        } else {
            return null;
        }
    }

    public float getMaxWireRange() {
        WireLineComponent component = this.getWireLineComponent();
        if(component == null) {
            return 0;
        } else {
            return component.maxWireLength();
        }
    }

    @Override
    public ItemStack getTheItem() {
        return this.item;
    }

    @Override
    public ItemStack splitTheItem(int amount) {
        ItemStack itemstack = this.item;
        this.setTheItem(ItemStack.EMPTY);
        this.setChanged();
        return itemstack;
    }

    @Override
    public void setTheItem(ItemStack item) {
        this.item = item;

        WireLineComponent component = this.getWireLineComponent();
        float oldDropoffRadius = this.wireLine.getDropoffRadius();
        float oldResistancePerBlock = this.wireLine.getResistancePerBlock();
        float newDropoffRadius = component == null ? 0 : component.rangeOfInfluence();
        float newResistancePerBlock = component == null ? 1F : component.resistancePerBlock();

        boolean needsAreaUpdate = false;
        if(oldDropoffRadius != newDropoffRadius) {
            this.wireLine.setDropoffRadius(newDropoffRadius);
            needsAreaUpdate = true;
        }
        if(oldResistancePerBlock != newResistancePerBlock) {
            this.wireLine.setResistancePerBlock(newResistancePerBlock);
        }

        this.updateLevelListStatus(needsAreaUpdate);
        this.setChanged();
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
