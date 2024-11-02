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
import phanastrae.hyphapiracea.block.entity.HyphalNodeBlockEntity;

public class HyphalNodeBlock extends BaseEntityBlock implements MiniCircuitHolder {
    public static final MapCodec<HyphalNodeBlock> CODEC = simpleCodec(HyphalNodeBlock::new);

    @Override
    public MapCodec<HyphalNodeBlock> codec() {
        return CODEC;
    }

    public HyphalNodeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HyphalNodeBlockEntity(pos, state);
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
