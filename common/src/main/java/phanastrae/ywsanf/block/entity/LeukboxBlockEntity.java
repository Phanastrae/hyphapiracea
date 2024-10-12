package phanastrae.ywsanf.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.structure.StructurePlacer;
import phanastrae.ywsanf.util.Timer;

public class LeukboxBlockEntity extends BlockEntity {

    private boolean active = false;
    private int timer = 0;
    private ResourceLocation structureRL;

    private String stage = "???";

    @Nullable
    private StructurePlacer structurePlacer = null;

    public LeukboxBlockEntity(BlockPos pos, BlockState blockState) {
        super(YWSaNFBlockEntityTypes.LEUKBOX, pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        if(nbt.contains("active", Tag.TAG_BYTE)) {
            this.active = nbt.getBoolean("active");
        }

        if(nbt.contains("stage", Tag.TAG_STRING)) {
            this.stage = nbt.getString("stage");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        nbt.putBoolean("active", this.active);

        nbt.putString("stage", this.structurePlacer == null ? "idle" : this.structurePlacer.getStage().getId());
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

    public void markForUpdate(ServerLevel world) {
        world.getChunkSource().blockChanged(this.getBlockPos());
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setStructureRL(ResourceLocation resourceLocation) {
        this.structureRL = resourceLocation;
    }

    public void startGeneratingStructure(ResourceLocation structureRL, BlockPos pos, ServerLevel serverLevel) {
        this.setActive(true);
        this.setStructureRL(structureRL);
        this.structurePlacer = new StructurePlacer(structureRL, pos);
        this.markForUpdate(serverLevel);
    }

    public Component getText() {
        if(this.active) {
            return Component.literal("Currently on Stage: " + this.stage);
        } else {
            return Component.literal("Idle");
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LeukboxBlockEntity blockEntity) {
        if(!(level instanceof ServerLevel serverLevel)) return;

        if(blockEntity.isActive()) {
            blockEntity.timer++;
            int timer = blockEntity.timer;

            if(timer % 10 == 0) {
                serverLevel.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 10, 0.2, 0.0, 0.2, 0.03);
            }

            StructurePlacer structurePlacer = blockEntity.structurePlacer;
            if(structurePlacer != null) {
                StructurePlacer.Stage placerStage = structurePlacer.getStage();

                if (timer >= placerStage.getWait()) {
                    Timer t = Timer.time(() -> structurePlacer.advance(serverLevel));
                    // TODO remove logging
                    YWSaNF.LOGGER.info("Advanced Stage {} to stage {}, this took {}μs ({}ms)", placerStage.getId(), structurePlacer.getStage().getId(), t.micro(), t.milli());

                    serverLevel.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 0.0, pos.getZ() + 0.5, placerStage.getWait() * 2, 0.6, 0.3, 0.6, 0.15);

                    blockEntity.timer = 0;
                    if (structurePlacer.getStage() == StructurePlacer.Stage.COMPLETED) {
                        blockEntity.active = false;
                        blockEntity.structurePlacer = null;
                    }
                }

                blockEntity.markForUpdate(serverLevel);
            }
        }
    }
}
