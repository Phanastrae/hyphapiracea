package phanastrae.hyphapiracea.block.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.block.LeukboxBlock;
import phanastrae.hyphapiracea.block.MiniCircuit;
import phanastrae.hyphapiracea.block.MiniCircuitHolder;
import phanastrae.hyphapiracea.component.HyphaPiraceaComponentTypes;
import phanastrae.hyphapiracea.component.type.DiscLockComponent;
import phanastrae.hyphapiracea.component.type.KeyedDiscComponent;
import phanastrae.hyphapiracea.electromagnetism.CircuitNetwork;
import phanastrae.hyphapiracea.electromagnetism.CircuitNode;
import phanastrae.hyphapiracea.electromagnetism.CircuitWire;
import phanastrae.hyphapiracea.particle.HyphaPiraceaParticleTypes;
import phanastrae.hyphapiracea.structure.StructurePlacer;
import phanastrae.hyphapiracea.structure.StructureType;
import phanastrae.hyphapiracea.structure.leubox_stages.*;
import phanastrae.hyphapiracea.structure.leubox_stages.AbstractLeukboxStage.LeukboxStage;
import phanastrae.hyphapiracea.world.HyphaPiraceaLevelAttachment;

public class LeukboxBlockEntity extends BlockEntity implements Clearable, ContainerSingleItem.BlockContainerSingleItem, MiniCircuitHolder {
    public static final String TAG_DISC_ITEM = "disc_item";
    public static final String TAG_DISC_RECOVERABLE = "disc_recoverable";
    public static final String TAG_PROGRESS = "progress";
    public static final String TAG_STAGE_PROGRESS = "stage_progress";
    public static final String TAG_STAGE_DATA = "stage_data";
    public static final String TAG_HAS_SUFFICIENT_POWER = "has_sufficient_power";
    public static final String TAG_POWER = "power";
    public static final String TAG_LEUKBOX_LOCK = "leukbox_lock";
    public static final String TAG_PREVENT_MANUAL_INTERACTION = "prevent_manual_interaction";

    private ItemStack item = ItemStack.EMPTY;
    private boolean discRecoverable = true;

    private AbstractLeukboxStage leukboxStage;
    private int progress = 0;
    private int stageProgress = 0;

    private final MiniCircuit miniCircuit;
    private final CircuitWire wire;
    private boolean hasSufficientPower;

    private double power;

    private String leukboxLock = "";
    private boolean preventManualInteraction = false;

    public LeukboxBlockEntity(BlockPos pos, BlockState blockState) {
        super(HyphaPiraceaBlockEntityTypes.PIRACEATIC_LEUKBOX, pos, blockState);

        this.leukboxStage = new IntroStage(pos, LeukboxStage.INTRO_SCREEN);

        this.miniCircuit = new MiniCircuit();
        CircuitNetwork network = new CircuitNetwork();
        CircuitNode circuitNode1 = new CircuitNode();
        circuitNode1.setNetwork(network);

        CircuitNode circuitNode2 = new CircuitNode();
        circuitNode2.setNetwork(network);

        this.wire = new CircuitWire(circuitNode1, circuitNode2, this.getInternalResistance(), 0);
        network.addWire(this.wire);
        this.miniCircuit.addInternalWire(this.wire);

        if(blockState.hasProperty(LeukboxBlock.FACING)) {
            Direction facing = blockState.getValue(LeukboxBlock.FACING);
            this.miniCircuit.setNode(facing.getClockWise(), circuitNode1);
            this.miniCircuit.setNode(facing.getCounterClockWise(), circuitNode2);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        if (nbt.contains(TAG_DISC_ITEM, 10)) {
            this.item = ItemStack.parse(registryLookup, nbt.getCompound(TAG_DISC_ITEM)).orElse(ItemStack.EMPTY);
        } else {
            this.item = ItemStack.EMPTY;
        }
        if(nbt.contains(TAG_DISC_ITEM, Tag.TAG_BYTE)) {
            this.discRecoverable = nbt.getBoolean(TAG_DISC_ITEM);
        }
        if(nbt.contains(TAG_PROGRESS, Tag.TAG_INT)) {
            this.progress = nbt.getInt(TAG_PROGRESS);
        }
        if(nbt.contains(TAG_STAGE_PROGRESS, Tag.TAG_INT)) {
            this.progress = nbt.getInt(TAG_STAGE_PROGRESS);
        }
        if(nbt.contains(TAG_HAS_SUFFICIENT_POWER, Tag.TAG_BYTE)) {
            this.hasSufficientPower = nbt.getBoolean(TAG_HAS_SUFFICIENT_POWER);
        }
        if(nbt.contains(TAG_LEUKBOX_LOCK, Tag.TAG_STRING)) {
            this.leukboxLock = nbt.getString(TAG_LEUKBOX_LOCK);
        } else {
            this.leukboxLock = "";
        }
        if(nbt.contains(TAG_PREVENT_MANUAL_INTERACTION, Tag.TAG_BYTE)) {
            this.preventManualInteraction = nbt.getBoolean(TAG_PREVENT_MANUAL_INTERACTION);
        } else {
            this.preventManualInteraction = false;
        }


        // TODO implement leukbox stage data serialization, currently reloading the leukbox will lose its structure spawning progress
        if(nbt.contains(TAG_STAGE_DATA, Tag.TAG_COMPOUND)) {
            CompoundTag stageData = nbt.getCompound(TAG_STAGE_DATA);

            // for now, assume the data is for a fake client stage since serialization is not implemented
            LeukboxStage fakeStage = LeukboxStage.IDLE;
            int currentSpawnTime = 0;
            int minSpawnTime = 0;
            String errorId = "";

            if(stageData.contains(AbstractLeukboxStage.TAG_FAKE_STAGE_ID, Tag.TAG_STRING)) {
                String s = stageData.getString(AbstractLeukboxStage.TAG_FAKE_STAGE_ID);
                for(LeukboxStage st : AbstractLeukboxStage.LeukboxStage.values()) {
                    if(s.equals(st.getId())) {
                        fakeStage = st;
                        break;
                    }
                }
            }
            if(stageData.contains(AbstractLeukboxStage.TAG_CURRENT_SPAWN_TIME, Tag.TAG_INT)) {
                currentSpawnTime = stageData.getInt(AbstractLeukboxStage.TAG_CURRENT_SPAWN_TIME);
            }
            if(stageData.contains(AbstractLeukboxStage.TAG_MIN_SPAWN_TIME, Tag.TAG_INT)) {
                minSpawnTime = stageData.getInt(AbstractLeukboxStage.TAG_MIN_SPAWN_TIME);
            }
            if(stageData.contains(AbstractLeukboxStage.TAG_ERROR_ID, Tag.TAG_STRING)) {
                errorId = stageData.getString(AbstractLeukboxStage.TAG_ERROR_ID);
            }

            FakeClientStage fakeClientStage = new FakeClientStage(
                    this.getBlockPos(),
                    fakeStage,
                    1,
                    currentSpawnTime,
                    minSpawnTime,
                    errorId
            );
            fakeClientStage.loadAdditional(stageData, registryLookup);
            this.leukboxStage = fakeClientStage;
        }

        if(nbt.contains(TAG_POWER, Tag.TAG_DOUBLE)) {
            this.power = nbt.getDouble(TAG_POWER);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        saveClientVisibleData(nbt, registryLookup);
    }

    protected void saveClientVisibleData(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        // save all the data that should also be sent to the client
        if (!this.getTheItem().isEmpty()) {
            nbt.put(TAG_DISC_ITEM, this.getTheItem().save(registryLookup));
        }
        nbt.putBoolean(TAG_DISC_RECOVERABLE, this.discRecoverable);
        nbt.putInt(TAG_PROGRESS, this.progress);
        nbt.putInt(TAG_STAGE_PROGRESS, this.stageProgress);
        nbt.putBoolean(TAG_HAS_SUFFICIENT_POWER, this.hasSufficientPower);

        if(!this.leukboxLock.isEmpty()) {
            nbt.putString(TAG_LEUKBOX_LOCK, this.leukboxLock);
        }
        if(this.preventManualInteraction) {
            nbt.putBoolean(TAG_PREVENT_MANUAL_INTERACTION, this.preventManualInteraction);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        CompoundTag nbtCompound = new CompoundTag();
        saveClientVisibleData(nbtCompound, registryLookup);

        FakeClientStage fakeClientStage = new FakeClientStage(
                this.getBlockPos(),
                this.getStage(),
                this.leukboxStage.getRequiredOperations(),
                this.getCurrentSpawnTime(),
                this.getCurrentMinSpawnTime(),
                this.getErrorId()
        );
        CompoundTag fakeClientStageData = new CompoundTag();
        fakeClientStage.saveAdditional(fakeClientStageData, registryLookup);
        nbtCompound.put(TAG_STAGE_DATA, fakeClientStageData);
        nbtCompound.putDouble(TAG_POWER, this.getPower());

        return nbtCompound;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LeukboxBlockEntity blockEntity) {
        if(blockEntity.miniCircuit.needsUpdate()) {
            MiniCircuitHolder.updateIfNeeded(level, pos, blockEntity.miniCircuit);
        }

        CircuitNetwork network = blockEntity.wire.getStartNode().getNetwork();
        CircuitNetwork network2 = blockEntity.wire.getEndNode().getNetwork();
        if(network != null) {
            network.tick(level.getGameTime());
        }
        if(network2 != null && network != network2) {
            network2.tick(level.getGameTime());
        }

        if(!(level instanceof ServerLevel serverLevel)) return;

        KeyedDiscComponent keyedDiscComponent = blockEntity.getDiscComponent();
        float requiredPower = keyedDiscComponent == null ? 0 : keyedDiscComponent.requiredPower();
        double currentPower = blockEntity.getPower();
        boolean hasSufficientPower = currentPower >= requiredPower;
        if(blockEntity.hasSufficientPower != hasSufficientPower || blockEntity.power != currentPower) {
            blockEntity.hasSufficientPower = hasSufficientPower;
            blockEntity.power = currentPower;

            blockEntity.setChanged();
            blockEntity.sendUpdate();
        }

        if(blockEntity.getStage() == LeukboxStage.IDLE) {
            if(!blockEntity.item.isEmpty()) {
                if (!blockEntity.discIsRecoverable()) {
                    blockEntity.setTheItem(ItemStack.EMPTY);
                    blockEntity.setDiscRecoverable(true);
                }
            }
        }

        if(blockEntity.hasSufficientPower) {
            AbstractLeukboxStage.LeukboxStage oldStage = blockEntity.getStage();
            if (blockEntity.tickProgress()) {
                blockEntity.setChanged();

                Vec3 magneticField = HyphaPiraceaLevelAttachment.getAttachment(level).getMagneticFieldAtPosition(pos.getCenter());

                //Timer t = new Timer();
                boolean stageChanged = blockEntity.tickPlacement(serverLevel, magneticField);
                //t.stop();
                LeukboxStage newStage = blockEntity.getStage();
                //HyphaPiracea.LOGGER.info("Advanced LeukboxStage {} to stage {}, this took {}μs ({}ms)", oldStage.getId(), newStage.getId(), t.micro(), t.milli());

                if (stageChanged) {
                    // play effects on feasting start
                    if ((oldStage == AbstractLeukboxStage.LeukboxStage.POST_PROCESS || oldStage == LeukboxStage.INSUFFICIENT_MAGNETIC_FIELD) && newStage == AbstractLeukboxStage.LeukboxStage.PLACE_BLOCKS) {
                        serverLevel.sendParticles(ParticleTypes.MYCELIUM, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 600, 0.4, 0, 0.4, 10.4);
                        serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 150, 0.5, 0.2, 0.5, 4.5);
                        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.PARROT_IMITATE_GHAST, SoundSource.BLOCKS, 4.5F, 0.3F);

                        blockEntity.setDiscRecoverable(false);
                    }

                    // play progress particles
                    if (newStage.isActive()) {
                        serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, oldStage.getWait() * 2, 0.5, 0.2, 0.5, 0.9);
                    }

                    blockEntity.sendUpdate();
                } else if (oldStage == LeukboxStage.ERROR) {
                    // stop error after a bit if item has been consumed
                    if (!blockEntity.discIsRecoverable() || blockEntity.item.isEmpty()) {
                        blockEntity.stopGeneratingStructure();
                        blockEntity.setTheItem(ItemStack.EMPTY);
                    }
                    blockEntity.sendUpdate();
                }
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, LeukboxBlockEntity blockEntity) {
        if(blockEntity.hasSufficientPower) {
            if (blockEntity.tickProgress()) {
                if (blockEntity.leukboxStage instanceof FakeClientStage fakeClientStage) {
                    fakeClientStage.advanceStage();
                }
            }
            RandomSource random = level.getRandom();
            AbstractLeukboxStage.LeukboxStage oldStage = blockEntity.getStage();
            if(oldStage != LeukboxStage.IDLE) {
                if (level.getGameTime() % 10 == 0) {
                    for(int i = 0; i < 20; i++) {
                        level.addParticle(
                                ParticleTypes.MYCELIUM,
                                pos.getX() + 0.5 + (random.nextFloat() * 2.0 - 1.0) * 0.3,
                                pos.getY() + 0.5 + (random.nextFloat() * 2.0 - 1.0) * 0.3,
                                pos.getZ() + 0.5 + (random.nextFloat() * 2.0 - 1.0) * 0.3,
                                (random.nextFloat() * 2.0 - 1.0) * 1.5,
                                (random.nextFloat() * 2.0 - 1.0) * 1.5,
                                (random.nextFloat() * 2.0 - 1.0) * 1.5
                        );
                    }
                }
            }

            if(blockEntity.getCurrentSpawnTime() > blockEntity.getCurrentMinSpawnTime()) {
                spawnSwirlingParticles(level.getRandom(), blockEntity.getCurrentSpawnTime(), pos, level);
            }
        }
    }

    public boolean tickProgress() {
        // return true if a new action should be taken
        int wait = this.getStage().getWait();
        if(wait == -1) return false;

        this.progress++;
        this.stageProgress++;
        if(this.stageProgress >= wait) {
            this.stageProgress = 0;
            return true;
        }
        return false;
    }

    public boolean tickPlacement(ServerLevel serverLevel, Vec3 magneticField) {
        KeyedDiscComponent discComponent = this.getDiscComponent();
        boolean isIntro = this.getStage() == LeukboxStage.INTRO_SCREEN || this.getStage() == LeukboxStage.INTRO_INITIALISING || this.getStage() == LeukboxStage.INTRO_LOADING || this.getStage() == LeukboxStage.INTRO_WELCOME;
        if(discComponent == null && !isIntro) {
            return false;
        }

        // return true if the stage changed
        AbstractLeukboxStage next = isIntro
                ? this.leukboxStage.advanceStage(serverLevel, magneticField, 0, 0)
                : this.leukboxStage.advanceStage(serverLevel, magneticField, discComponent.maxOperatingRadius(), discComponent.minOperatingTesla());
        if (next != this.leukboxStage) {
            this.leukboxStage = next;
            this.progress = 0;
            return true;
        }
        return false;
    }

    public void sendUpdate() {
        if(this.level != null && !this.level.isClientSide && this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 2);
        }
    }

    public static void spawnSwirlingParticles(RandomSource random, int currentSpawnTime, BlockPos pos, Level level) {
        for(int i = 0; i < 45; i++) {
            double dy = random.nextDouble() * 120 - 60;
            double noise = random.nextDouble() * (StructurePlacer.MAX_NOISE_DELAY - 1);

            double radius = (currentSpawnTime - StructurePlacer.CONVERSION_DELAY - 0.9F * -dy - noise * -4.5F) / 7.0F;

            if(radius > 0) {
                double theta = random.nextDouble() * Math.TAU;
                double dx = Math.cos(theta) * radius;
                double dz = Math.sin(theta) * radius;

                BlockPos p = BlockPos.containing(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                BlockState s = level.getBlockState(p);
                if(s.isAir() || s.canBeReplaced()) {
                    level.addParticle(
                            random.nextFloat() > 0.4 ? HyphaPiraceaParticleTypes.LARGE_ELECTROMAGNETIC_DUST : HyphaPiraceaParticleTypes.LARGE_FAIRY_FOG,
                            random.nextInt(3) == 0,
                            pos.getX() + dx,
                            pos.getY() + dy,
                            pos.getZ() + dz,
                            random.nextFloat() - 0.5,
                            random.nextFloat() - 0.5,
                            random.nextFloat() - 0.5
                    );
                }
            }
        }
    }

    public void startGeneratingStructure(ResourceLocation structureRL, StructureType structureType, boolean rotateStructure, BlockPos pos) {
        this.leukboxStage = new GetStructureStage(pos, structureRL, structureType, rotateStructure);
    }

    public void stopGeneratingStructure() {
        this.leukboxStage = new IdleLeukboxStage(this.getBlockPos());
    }

    public Component getTopText() {
        LeukboxStage stage = this.getStage();
        String langKey = "hyphapiracea.leukbox.stage." + stage.getId();
        return Component.translatable(langKey).withStyle(stage.getFormats());
    }

    @Nullable
    public Component getBottomText() {
        if(this.getStage() == LeukboxStage.ERROR) {
            return Component.translatable("hyphapiracea.leukbox.error." + this.getErrorId()).withStyle(ChatFormatting.RED);
        } else if(this.getStage() == LeukboxStage.INTRO_SCREEN) {
            return Component.translatable("hyphapiracea.leukbox.subtitle.intro_screen", Component.translatable("hyphapiracea.leukbox.subtitle.intro_screen.of").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.LIGHT_PURPLE)).withStyle(ChatFormatting.DARK_PURPLE);
        } else if(this.getStage() == LeukboxStage.INTRO_WELCOME) {
            return Component.translatable("hyphapiracea.leukbox.subtitle.intro_welcome").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC);
        } else if(this.getStage() == LeukboxStage.IDLE) {
            return Component.translatable("hyphapiracea.leukbox.subtitle.idle").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC);
        } else {
            if (this.getStage().shouldShowProgress()) {
                return Component.translatable("hyphapiracea.leukbox.progress", this.getProgressPercent()).withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC);
            } else {
                return null;
            }
        }
    }

    public Component getStructureText() {
        if(this.getStage() == LeukboxStage.IDLE) {
            if(!this.leukboxLock.isEmpty()) {
                return Component.translatable("hyphapiracea.leukbox.lock.current", this.leukboxLock).withStyle(ChatFormatting.RED);
            }
        }

        if(!this.getStage().isActive()) {
            return null;
        }

        KeyedDiscComponent keyedDiscComponent = this.getDiscComponent();
        if(keyedDiscComponent != null) {
            String lang = keyedDiscComponent.structureType() == StructureType.STRUCTURE ? "hyphapiracea.leukbox.structure" : "hyphapiracea.leukbox.template";
            return Component.translatable(lang, keyedDiscComponent.structureId().toString()).withStyle(ChatFormatting.DARK_GRAY);
        } else {
            return null;
        }
    }

    public Component getFieldStrengthText() {
        if(!this.getStage().isActive()) {
            return null;
        }

        KeyedDiscComponent keyedDiscComponent = this.getDiscComponent();
        if(this.level != null) {
            double currentStrength = HyphaPiraceaLevelAttachment.getAttachment(this.level).getMagneticFieldAtPosition(this.getBlockPos().getCenter()).length();
            double minOperatingTesla = keyedDiscComponent == null ? 0 : keyedDiscComponent.minOperatingTesla();
            double ratio = (minOperatingTesla <= 0) ? 10 : currentStrength / minOperatingTesla;
            ChatFormatting color;
            if(ratio < 0.25) {
                color = ChatFormatting.RED;
            } else if(ratio < 1) {
                color = ChatFormatting.YELLOW;
            } else if(ratio < 1.2) {
                color = ChatFormatting.GREEN;
            } else {
                color = ChatFormatting.AQUA;
            }

            double currentStrengthMicroTesla = currentStrength * 1E6;
            String string1 = String.format("%1$,.3f", currentStrengthMicroTesla);

            double requiredStrengthMicroTesla = minOperatingTesla * 1E6;
            String string2 = String.format("%1$,.3f", requiredStrengthMicroTesla);

            return Component.translatable("hyphapiracea.leukbox.stats.field_strength", string1, string2).withStyle(color);
        } else {
            return null;
        }
    }

    @Nullable
    public Component getPowerText() {
        if(!this.getStage().isActive()) {
            return null;
        }

        KeyedDiscComponent keyedDiscComponent = this.getDiscComponent();
        if(this.level != null) {
            double currentPower = this.power;
            double requiredPower = keyedDiscComponent == null ? 0 : keyedDiscComponent.requiredPower();
            double ratio = (requiredPower <= 0) ? 10 : currentPower / requiredPower;
            ChatFormatting color;
            if(ratio < 0.25) {
                color = ChatFormatting.RED;
            } else if(ratio < 1) {
                color = ChatFormatting.YELLOW;
            } else if(ratio < 1.2) {
                color = ChatFormatting.GREEN;
            } else {
                color = ChatFormatting.AQUA;
            }

            String string1 = String.format("%1$,.3f", currentPower);
            String string2 = String.format("%1$,.3f", requiredPower);

            return Component.translatable("hyphapiracea.leukbox.stats.power", string1, string2).withStyle(color);
        } else {
            return null;
        }
    }

    public AbstractLeukboxStage.LeukboxStage getStage() {
        return this.leukboxStage.getStage();
    }

    public int getProgressPercent() {
        int maxProgress = this.getStage().getWait() * this.leukboxStage.getRequiredOperations();
        if(maxProgress <= 0) {
            return 100;
        } else {
            float progressFraction = this.progress / (float)maxProgress;
            return Math.clamp(Math.round(progressFraction * 100), 0, 100);
        }
    }

    public int getCurrentSpawnTime() {
        if(this.leukboxStage instanceof SpawnTimeHolder sth) {
            return sth.getCurrentSpawnTime();
        } else {
            return 0;
        }
    }

    public int getCurrentMinSpawnTime() {
        if(this.leukboxStage instanceof SpawnTimeHolder sth) {
            return sth.getMinSpawnTime();
        } else {
            return 0;
        }
    }

    public String getErrorId() {
        if(this.leukboxStage instanceof ErrorIdHolder eih) {
            return eih.getErrorId();
        } else {
            return "";
        }
    }

    public void setDiscRecoverable(boolean recoverable) {
        this.discRecoverable = recoverable;
        this.setChanged();
    }

    public boolean discIsRecoverable() {
        return this.discRecoverable;
    }

    public void popOutTheItem() {
        if (this.level != null && !this.level.isClientSide) {
            BlockPos blockpos = this.getBlockPos();
            ItemStack itemstack = this.getTheItem();
            if (!itemstack.isEmpty()) {
                this.removeTheItem();
                this.stopGeneratingStructure();
                this.setChanged();

                if(discIsRecoverable()) {
                    Vec3 vec3 = Vec3.atLowerCornerWithOffset(blockpos, 0.5, 1.01, 0.5).offsetRandom(this.level.random, 0.7F);
                    ItemStack itemstack1 = itemstack.copy();
                    ItemEntity itementity = new ItemEntity(this.level, vec3.x(), vec3.y(), vec3.z(), itemstack1);
                    itementity.setDefaultPickUpDelay();
                    this.level.addFreshEntity(itementity);
                }
            }
        }
    }

    private void notifyItemChanged(boolean hasDisc) {
        if (this.level != null && this.level.getBlockState(this.getBlockPos()) == this.getBlockState()) {
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(LeukboxBlock.HAS_DISC, hasDisc), 2);
            this.level.gameEvent(GameEvent.BLOCK_CHANGE, this.getBlockPos(), GameEvent.Context.of(this.getBlockState()));
        }
    }

    @Nullable
    public KeyedDiscComponent getDiscComponent() {
        if(this.item.has(HyphaPiraceaComponentTypes.KEYED_DISC_COMPONENT)) {
            return this.item.get(HyphaPiraceaComponentTypes.KEYED_DISC_COMPONENT);
        } else {
            return null;
        }
    }

    @Override
    public ItemStack getTheItem() {
        return this.item;
    }

    @Override
    public ItemStack splitTheItem(int amount) {
        this.setChanged();

        ItemStack itemstack = this.item;
        this.setTheItem(ItemStack.EMPTY);
        return itemstack;
    }

    @Override
    public void setTheItem(ItemStack item) {
        this.setChanged();

        this.item = item;
        boolean flag = !this.item.isEmpty();
        this.notifyItemChanged(flag);

        KeyedDiscComponent component = item.get(HyphaPiraceaComponentTypes.KEYED_DISC_COMPONENT);
        if(component != null) {
            this.startGeneratingStructure(component.structureId(), component.structureType(), component.rotateStructure(), this.getBlockPos());
            this.setChanged();
        }
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
        String stackLock = DiscLockComponent.getDiscLockFromDisc(stack);
        String thisLock = this.leukboxLock;
        return stackLock.equals(thisLock) && stack.has(HyphaPiraceaComponentTypes.KEYED_DISC_COMPONENT);
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        if(!this.discIsRecoverable()) {
            return false;
        }
        if(this.getStage() != LeukboxStage.IDLE && this.getStage() != LeukboxStage.ERROR) {
            return false;
        }
        return target.hasAnyMatching(ItemStack::isEmpty);
    }

    public float getInternalResistance() {
        return 1F;
    }

    public double getPower() {
        return this.wire.getPowerDissipation();
    }

    @Override
    public void setRemoved() {
        this.miniCircuit.onRemoved();
        super.setRemoved();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        this.miniCircuit.onUnremoved();
    }

    @Override
    @Nullable
    public MiniCircuit getMiniCircuit(BlockGetter blockGetter, BlockPos pos, BlockState state, Direction side) {
        if(side.getAxis().isVertical()) {
            return null;
        }

        if(state.hasProperty(LeukboxBlock.FACING)) {
            Direction direction = state.getValue(LeukboxBlock.FACING);

            if(direction == side || direction == side.getOpposite()) {
                return null;
            }

            return this.miniCircuit;
        }

        return null;
    }

    public void setLeukboxLock(String leukboxLock) {
        this.leukboxLock = leukboxLock;
        this.setChanged();
        this.sendUpdate();
    }

    public String getLeukboxLock() {
        return leukboxLock;
    }

    public void setPreventManualInteraction(boolean preventManualInteraction) {
        this.preventManualInteraction = preventManualInteraction;
        this.setChanged();
        this.sendUpdate();
    }

    public boolean shouldPreventManualInteraction() {
        return this.preventManualInteraction;
    }
}
