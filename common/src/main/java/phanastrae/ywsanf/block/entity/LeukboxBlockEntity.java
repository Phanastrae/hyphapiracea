package phanastrae.ywsanf.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import phanastrae.ywsanf.structure.StructurePlacement;

public class LeukboxBlockEntity extends BlockEntity {

    private boolean active = false;
    private int timer = 0;

    public LeukboxBlockEntity(BlockPos pos, BlockState blockState) {
        super(YWSaNFBlockEntityTypes.LEUKBOX, pos, blockState);
    }

    public void setActive() {
        this.active = true;
    }

    public boolean isActive() {
        return this.active;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LeukboxBlockEntity blockEntity) {
        if(!(level instanceof ServerLevel serverLevel)) return;

        if(blockEntity.isActive()) {
            blockEntity.timer++;
            if(blockEntity.timer % 10 == 0) {
                serverLevel.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 10, 0.3, 0.1, 0.3, 0.03);
            }
            if(blockEntity.timer > 200) {
                blockEntity.timer = 0;
                blockEntity.active = false;

                StructurePlacement.placeStructure(serverLevel, pos);
            }
        }
    }
}
