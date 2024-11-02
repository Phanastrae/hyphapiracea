package phanastrae.hyphapiracea.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.block.entity.GalvanocarpicBulbBlockEntity;

public class GalvanocarpicBulbBlock extends BaseEntityBlock implements MiniCircuitHolder {
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
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);

        for(Direction direction : Direction.values()) {
            MiniCircuit mc = this.getMiniCircuit(level, pos, state, direction);
            if(mc != null) {
                mc.bindToNeighbors(level, pos);
            }
        }
    }

    @Override
    public @Nullable MiniCircuit getMiniCircuit(Level level, BlockPos pos, BlockState state, Direction side) {
        if(level.getBlockEntity(pos) instanceof MiniCircuitHolder cnh) {
            return cnh.getMiniCircuit(level, pos, state, side);
        } else {
            return null;
        }
    }
}
