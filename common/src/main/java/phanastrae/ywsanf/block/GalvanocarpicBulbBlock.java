package phanastrae.ywsanf.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.block.entity.GalvanocarpicBulbBlockEntity;
import phanastrae.ywsanf.electromagnetism.ChargeSac;

public class GalvanocarpicBulbBlock extends BaseEntityBlock implements ChargeSacContainer {
    public static final MapCodec<GalvanocarpicBulbBlock> CODEC = simpleCodec(GalvanocarpicBulbBlock::new);

    @Override
    public MapCodec<GalvanocarpicBulbBlock> codec() {
        return CODEC;
    }

    public GalvanocarpicBulbBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GalvanocarpicBulbBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return null;
    }

    @Override
    @Nullable
    public ChargeSac getChargeSac(Level level, BlockPos pos, BlockState state, Direction side) {
        if(level.getBlockEntity(pos) instanceof GalvanocarpicBulbBlockEntity blockEntity) {
            return blockEntity.getChargeSac();
        } else {
            return null;
        }
    }
}
