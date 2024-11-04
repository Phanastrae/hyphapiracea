package phanastrae.hyphapiracea.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import phanastrae.hyphapiracea.item.HyphaPiraceaItems;

public class AzimulicStemBlock extends Block {
    public static final MapCodec<AzimulicStemBlock> CODEC = simpleCodec(AzimulicStemBlock::new);
    public static final EnumProperty<Direction.Axis> AXIS  = BlockStateProperties.AXIS;

    @Override
    public MapCodec<AzimulicStemBlock> codec() {
        return CODEC;
    }

    public AzimulicStemBlock(Properties properties) {
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(AXIS, context.getClickedFace().getAxis());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Direction direction = hitResult.getDirection();
        Direction.Axis axis = state.getValue(AXIS);
        if(axis.test(direction)) {
            if (player.mayBuild() && stack.is(HyphaPiraceaItems.HYPHALINE)) {
                if (!level.isClientSide) {
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, pos, stack);
                    }

                    BlockState newState = HyphaPiraceaBlocks.HYPHAL_STEM.defaultBlockState().setValue(AXIS, axis);
                    level.setBlock(pos, newState, 11);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));

                    stack.consume(1, player);
                }
                level.playSound(player, pos, SoundEvents.ITEM_FRAME_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
