package phanastrae.ywsanf.block.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.block.LeukboxBlock;
import phanastrae.ywsanf.item.YWSaNFItems;
import phanastrae.ywsanf.particle.YWSaNFParticleTypes;
import phanastrae.ywsanf.structure.StructurePlacer;
import phanastrae.ywsanf.util.Timer;

import java.util.List;

public class LeukboxBlockEntity extends BlockEntity implements Clearable, ContainerSingleItem.BlockContainerSingleItem {
    public static final String TAG_DISC_ITEM = "disc_item";

    private ItemStack item = ItemStack.EMPTY;

    private int timer = 0;

    private StructurePlacer.Stage stage = StructurePlacer.Stage.IDLE;
    private int progressPercent = 0;

    @Nullable
    private StructurePlacer structurePlacer = null;

    private BoundingBox[] boxes = new BoundingBox[0];
    private int currentSpawnTime;
    private int currentMinSpawnTime;

    public LeukboxBlockEntity(BlockPos pos, BlockState blockState) {
        super(YWSaNFBlockEntityTypes.LEUKBOX, pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        if (nbt.contains(TAG_DISC_ITEM, 10)) {
            this.item = ItemStack.parse(registryLookup, nbt.getCompound(TAG_DISC_ITEM)).orElse(ItemStack.EMPTY);
        } else {
            this.item = ItemStack.EMPTY;
        }

        if(nbt.contains("stage", Tag.TAG_STRING)) {
            String s = nbt.getString("stage");
            for(StructurePlacer.Stage st : StructurePlacer.Stage.values()) {
                if(s.equals(st.getId())) {
                    this.stage = st;
                    break;
                }
            }
        }
        if(nbt.contains("progress_percent", Tag.TAG_INT)) {
            int p = nbt.getInt("progress_percent");
            if(0 <= p && p <= 100) {
                this.progressPercent = p;
            }
        }
        if(nbt.contains("box_data", Tag.TAG_INT_ARRAY)) {
            int[] boxData = nbt.getIntArray("box_data");
            if(boxData.length > 0 && boxData.length % 6 == 0) {
                int boxCount = boxData.length / 6;
                BoundingBox[] boxes = new BoundingBox[boxCount];
                for(int i = 0; i < boxCount; i++) {
                    int k = i * 6;
                    int minX = boxData[k];
                    int minY = boxData[k+1];
                    int minZ = boxData[k+2];
                    int maxX = boxData[k+3];
                    int maxY = boxData[k+4];
                    int maxZ = boxData[k+5];
                    BoundingBox box = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
                    boxes[i] = box;
                }
                this.boxes = boxes;
            }
        }

        if(nbt.contains("current_spawn_time", Tag.TAG_INT)) {
            this.currentSpawnTime = nbt.getInt("current_spawn_time");
        }

        if(nbt.contains("current_min_spawn_time", Tag.TAG_INT)) {
            this.currentMinSpawnTime = nbt.getInt("current_min_spawn_time");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        if (!this.getTheItem().isEmpty()) {
            nbt.put(TAG_DISC_ITEM, this.getTheItem().save(registryLookup));
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        CompoundTag nbtCompound = this.saveCustomOnly(registryLookup);
        // TODO optimise, don't send the entire data every time
        nbtCompound.putString("stage", this.structurePlacer == null ? "idle" : this.structurePlacer.getStage().getId());
        nbtCompound.putInt("progress_percent", this.structurePlacer == null ? 0 : this.structurePlacer.getProgressPercent());

        if(this.structurePlacer != null) {
            List<StructurePiece> pieces = this.structurePlacer.getPiecesNoRemoval();
            if (!pieces.isEmpty()) {
                int[] boxCoords = new int[pieces.size() * 6];
                for (int i = 0; i < pieces.size(); i++) {
                    BoundingBox box = pieces.get(i).getBoundingBox();
                    int k = i * 6;
                    boxCoords[k] = box.minX();
                    boxCoords[k + 1] = box.minY();
                    boxCoords[k + 2] = box.minZ();
                    boxCoords[k + 3] = box.maxX();
                    boxCoords[k + 4] = box.maxY();
                    boxCoords[k + 5] = box.maxZ();
                }

                nbtCompound.putIntArray("box_data", boxCoords);
            }

            if(this.structurePlacer.getStage() == StructurePlacer.Stage.PLACE_BLOCKS) {
                nbtCompound.putInt("current_spawn_time", structurePlacer.getCurrentSpawnTime());
                nbtCompound.putInt("current_min_spawn_time", structurePlacer.getCurrentMinSpawnTime());
            }
        }

        return nbtCompound;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void markForUpdate(ServerLevel world) {
        world.getChunkSource().blockChanged(this.getBlockPos());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LeukboxBlockEntity blockEntity) {
        if(!(level instanceof ServerLevel serverLevel)) return;

        StructurePlacer structurePlacer = blockEntity.structurePlacer;
        if(structurePlacer != null) {
            StructurePlacer.Stage oldStage = structurePlacer.getStage();
            if(level.getGameTime() % 10 == 0) {
                serverLevel.sendParticles(ParticleTypes.MYCELIUM, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 20, 0.3, 0.0, 0.3, 1.5);
            }


            Timer t = new Timer();
            boolean didAdvance = structurePlacer.tick(serverLevel);
            t.stop();

            if(didAdvance) {
                StructurePlacer.Stage newStage = structurePlacer.getStage();
                if(oldStage == StructurePlacer.Stage.POST_PROCESS && newStage == StructurePlacer.Stage.PLACE_BLOCKS) {
                    serverLevel.sendParticles(ParticleTypes.MYCELIUM, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 600, 0.4, 0, 0.4, 10.4);
                    serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, 150, 0.5, 0.2, 0.5, 4.5);
                    level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.PARROT_IMITATE_GHAST, SoundSource.BLOCKS, 4.5F, 0.3F);
                }

                if(oldStage != StructurePlacer.Stage.PLACE_SPECIALS) {
                    // TODO remove logging
                    YWSaNF.LOGGER.info("Advanced Stage {} to stage {}, this took {}μs ({}ms)", oldStage.getId(), newStage.getId(), t.micro(), t.milli());

                    serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, oldStage.getWait() * 2, 0.5, 0.2, 0.5, 0.9);
                } else {
                    blockEntity.structurePlacer = null;
                    blockEntity.stage = StructurePlacer.Stage.IDLE;

                    blockEntity.setTheItem(ItemStack.EMPTY);
                }
            }

            blockEntity.markForUpdate(serverLevel); // TODO tweak this perhaps, is it needed?
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, LeukboxBlockEntity blockEntity) {
        if(blockEntity.stage == StructurePlacer.Stage.PLACE_BLOCKS) {
            for (BoundingBox box : blockEntity.boxes) {
                int volume = box.getXSpan() * box.getYSpan() * box.getZSpan();
                int particles = Mth.ceil(Math.min(Math.pow(volume, 0.6667), 30F / blockEntity.boxes.length));

                if (particles > 0) {
                    RandomSource random = level.getRandom();

                    for (int i = 0; i < particles; i++) {
                        level.addParticle(
                                random.nextBoolean() ? ParticleTypes.WAX_ON : ParticleTypes.WAX_OFF,
                                random.nextInt(8) == 0,
                                box.minX() + box.getXSpan() * random.nextFloat(),
                                box.minY() + box.getYSpan() * random.nextFloat(),
                                box.minZ() + box.getZSpan() * random.nextFloat(),
                                random.nextFloat() - 0.5,
                                random.nextFloat() - 0.5,
                                random.nextFloat() - 0.5
                        );
                    }
                }
            }
        }

        if(blockEntity.currentSpawnTime > blockEntity.currentMinSpawnTime) {
            RandomSource random = level.random;
            for(int i = 0; i < 120; i++) {
                double dy = random.nextDouble() * 120 - 60;
                double noise = random.nextDouble() * (StructurePlacer.MAX_NOISE_DELAY - 1);

                double radius = (blockEntity.currentSpawnTime - StructurePlacer.CONVERSION_DELAY - 0.9F * -dy - noise * -4.5F) / 7.0F;

                if(radius > 0) {
                    double theta = random.nextDouble() * Math.TAU;
                    double dx = Math.cos(theta) * radius;
                    double dz = Math.sin(theta) * radius;

                    BlockPos p = BlockPos.containing(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    BlockState s = level.getBlockState(p);
                    if(s.isAir() || s.canBeReplaced()) {
                        level.addParticle(
                                random.nextFloat() > 0.4 ? YWSaNFParticleTypes.LARGE_ELECTROMAGNETIC_DUST : YWSaNFParticleTypes.LARGE_FAIRY_FOG,
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

            blockEntity.currentSpawnTime--;
        }
    }

    public void startGeneratingStructure(ResourceLocation structureRL, BlockPos pos, ServerLevel serverLevel) {
        this.structurePlacer = new StructurePlacer(structureRL, pos);
        this.markForUpdate(serverLevel); // TODO is this needed
    }

    public Component getTopText() {
        String langKey = "ywsanf.leukbox.stage." + this.stage.getId();
        MutableComponent component = Component.translatable(langKey);
        switch(this.stage) {
            case PLACE_BLOCKS, PLACE_SPECIALS -> component.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            case COMPLETED -> component.withStyle(ChatFormatting.GREEN);
            case ERROR -> component.withStyle(ChatFormatting.RED);
        }

        return component;
    }

    @Nullable
    public Component getBottomText() {
        if(this.stage.shouldShowProgress()) {
            return Component.translatable("ywsanf.leukbox.progress", this.progressPercent).withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC);
        } else {
            return null;
        }
    }

    public boolean discIsRecoverable() {
        if(this.structurePlacer == null) {
            return true;
        }
        return switch (this.structurePlacer.getStage()) {
            case PLACE_BLOCKS, PLACE_SPECIALS, COMPLETED -> false;
            default -> true;
        };
    }

    public void popOutTheItem() {
        if (this.level != null && !this.level.isClientSide) {
            BlockPos blockpos = this.getBlockPos();
            ItemStack itemstack = this.getTheItem();
            if (!itemstack.isEmpty()) {
                this.removeTheItem();

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
        boolean flag = !this.item.isEmpty();
        this.notifyItemChanged(flag);
        // TODO
        Component component = item.get(DataComponents.CUSTOM_NAME);
        if(component != null) {
            String s = component.getString();
            ResourceLocation rl = ResourceLocation.tryParse(s);
            if(rl != null) {
                if(level instanceof ServerLevel serverLevel) {
                    this.startGeneratingStructure(rl, this.getBlockPos(), serverLevel);
                }
            }
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
        // TODO change
        return stack.is(YWSaNFItems.KEYED_DISC);
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return target.hasAnyMatching(ItemStack::isEmpty);
    }
}
