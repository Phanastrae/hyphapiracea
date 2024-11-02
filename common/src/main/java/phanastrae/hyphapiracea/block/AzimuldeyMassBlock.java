package phanastrae.hyphapiracea.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.block.entity.HyphalNodeBlockEntity;
import phanastrae.hyphapiracea.electromagnetism.CircuitNode;
import phanastrae.hyphapiracea.item.HyphaPiraceaItems;

import java.util.Map;

public class AzimuldeyMassBlock extends Block {
    public static final MapCodec<AzimuldeyMassBlock> CODEC = simpleCodec(AzimuldeyMassBlock::new);
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty UP = PipeBlock.UP;
    public static final BooleanProperty DOWN = PipeBlock.DOWN;
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;

    @Override
    public MapCodec<AzimuldeyMassBlock> codec() {
        return CODEC;
    }

    public AzimuldeyMassBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(NORTH, false)
                        .setValue(EAST, false)
                        .setValue(SOUTH, false)
                        .setValue(WEST, false)
                        .setValue(UP, false)
                        .setValue(DOWN, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(UP, DOWN, NORTH, EAST, SOUTH, WEST);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.NORTH)), state.getValue(NORTH))
                .setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.SOUTH)), state.getValue(SOUTH))
                .setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.EAST)), state.getValue(EAST))
                .setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.WEST)), state.getValue(WEST))
                .setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.UP)), state.getValue(UP))
                .setValue(PROPERTY_BY_DIRECTION.get(rotation.rotate(Direction.DOWN)), state.getValue(DOWN));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.NORTH)), state.getValue(NORTH))
                .setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.SOUTH)), state.getValue(SOUTH))
                .setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.EAST)), state.getValue(EAST))
                .setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.WEST)), state.getValue(WEST))
                .setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.UP)), state.getValue(UP))
                .setValue(PROPERTY_BY_DIRECTION.get(mirror.mirror(Direction.DOWN)), state.getValue(DOWN));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return AzimuldeyMassBlock.getPlacementState(context.getLevel(), context.getClickedPos(), this.defaultBlockState());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Direction direction = hitResult.getDirection();
        BooleanProperty property = PROPERTY_BY_DIRECTION.get(direction);
        if(!state.getValue(property)) {
            if(player.mayBuild() && stack.is(ItemTags.AXES)) {
                if (!level.isClientSide) {
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, pos, stack);
                    }

                    BlockState newState = state.setValue(property, true);
                    level.setBlock(pos, newState, 11);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));

                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));

                    if(level.getBlockEntity(pos) instanceof HyphalNodeBlockEntity hnbe) {
                        hnbe.addSide(direction);
                    }
                }
                level.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        } else {
            if(player.mayBuild() && stack.is(HyphaPiraceaItems.HYPHALINE)) {
                if (!level.isClientSide) {
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, pos, stack);
                    }

                    BlockState newState = HyphaPiraceaBlocks.HYPHAL_NODE.defaultBlockState();
                    for(Direction d : Direction.values()) {
                        BooleanProperty prop = PROPERTY_BY_DIRECTION.get(d);
                        newState = newState.setValue(prop, state.getValue(prop));
                    }
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

    public static BlockState getPlacementState(BlockGetter blockGetter, BlockPos thisPos, BlockState thisState) {
        for(Direction direction : Direction.values()) {
            BlockPos adjPos = thisPos.offset(direction.getNormal());
            BlockState adjState = blockGetter.getBlockState(adjPos);
            if(adjState.getBlock() instanceof MiniCircuitHolder mch) {
                MiniCircuit mc = mch.getMiniCircuit(blockGetter, adjPos, adjState, direction.getOpposite());
                if(mc != null) {
                    CircuitNode node = mc.getNode(direction.getOpposite());
                    if(node != null) {
                        thisState = thisState.setValue(PROPERTY_BY_DIRECTION.get(direction), true);
                    }
                }
            } else if(adjState.getBlock() instanceof AzimuldeyMassBlock) {
                BooleanProperty dirProp = PROPERTY_BY_DIRECTION.get(direction.getOpposite());
                if(adjState.hasProperty(dirProp)) {
                    if(adjState.getValue(dirProp)) {
                        thisState = thisState.setValue(PROPERTY_BY_DIRECTION.get(direction), true);
                    }
                }
            }
        }

        return thisState;
    }
}
