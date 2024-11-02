package phanastrae.hyphapiracea.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractTwoSidedChargeSacBlock extends BaseEntityBlock implements MiniCircuitHolder {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;

    public AbstractTwoSidedChargeSacBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
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
    public @Nullable MiniCircuit getMiniCircuit(BlockGetter blockGetter, BlockPos pos, BlockState state, Direction side) {
        if(blockGetter.getBlockEntity(pos) instanceof MiniCircuitHolder cnh) {
            return cnh.getMiniCircuit(blockGetter, pos, state, side);
        } else {
            return null;
        }
    }
}
