package phanastrae.hyphapiracea.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.block.entity.HyphaPiraceaBlockEntityTypes;
import phanastrae.hyphapiracea.block.entity.HyphalStemBlockEntity;

public class HyphalStemBlock extends BaseEntityBlock implements MiniCircuitHolder {
    public static final MapCodec<HyphalStemBlock> CODEC = simpleCodec(HyphalStemBlock::new);
    public static final EnumProperty<Direction.Axis> AXIS  = BlockStateProperties.AXIS;

    @Override
    public MapCodec<HyphalStemBlock> codec() {
        return CODEC;
    }

    public HyphalStemBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(AXIS, Direction.Axis.Y)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return rotatePillar(state, rot);
    }

    public static BlockState rotatePillar(BlockState state, Rotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                switch ((Direction.Axis)state.getValue(AXIS)) {
                    case X:
                        return state.setValue(AXIS, Direction.Axis.Z);
                    case Z:
                        return state.setValue(AXIS, Direction.Axis.X);
                    default:
                        return state;
                }
            default:
                return state;
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HyphalStemBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide
                ? null
                : createTickerHelper(blockEntityType, HyphaPiraceaBlockEntityTypes.HYPHAL_STEM, HyphalStemBlockEntity::serverTick);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
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
