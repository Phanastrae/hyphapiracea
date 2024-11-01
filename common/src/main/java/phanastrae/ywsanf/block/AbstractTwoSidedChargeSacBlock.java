package phanastrae.ywsanf.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.block.entity.AbstractTwoSidedChargeSacBlockEntity;
import phanastrae.ywsanf.electromagnetism.CircuitNode;

public abstract class AbstractTwoSidedChargeSacBlock extends BaseEntityBlock implements CircuitNodeHolder {
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
    @Nullable
    public CircuitNode getCircuitNode(Level level, BlockPos pos, BlockState state, Direction side) {
        if(state.hasProperty(FACING)) {
            Direction direction = state.getValue(FACING);

            if(level.getBlockEntity(pos) instanceof AbstractTwoSidedChargeSacBlockEntity blockEntity) {
                if(direction == side) {
                    return blockEntity.getPositiveCircuitNode();
                } else if(direction == side.getOpposite()) {
                    return blockEntity.getNegativeCircuitNode();
                } else {
                    return null;
                }
            }
        }

        return null;
    }
}
