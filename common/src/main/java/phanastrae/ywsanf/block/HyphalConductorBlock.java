package phanastrae.ywsanf.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.block.entity.HyphalConductorBlockEntity;
import phanastrae.ywsanf.block.entity.YWSaNFBlockEntityTypes;
import phanastrae.ywsanf.block.state.ConductorStateProperty;
import phanastrae.ywsanf.block.state.YWSaNFBlockProperties;
import phanastrae.ywsanf.component.YWSaNFComponentTypes;
import phanastrae.ywsanf.entity.YWSaNFEntityAttachment;

public class HyphalConductorBlock extends BaseEntityBlock {
    private static final MapCodec<HyphalConductorBlock> CODEC = simpleCodec(HyphalConductorBlock::new);

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty STICKY = YWSaNFBlockProperties.STICKY;
    public static final ConductorStateProperty CONDUCTOR_STATE = YWSaNFBlockProperties.CONDUCTOR_WIRE_STATE;

    protected static final VoxelShape SMALL_COIL_AABB = Block.box(6.0, 6.0, 6.0, 10.0, 10.0, 10.0);
    protected static final VoxelShape FULL_COIL_AABB = Block.box(6.0, 3.0, 6.0, 10.0, 13.0, 10.0);
    protected static final VoxelShape UP_AABB = Shapes.join(
            Block.box(5.0, 0.0, 5.0, 11.0, 3.0, 11.0),
            Block.box(7.0, 3.0, 7.0, 9.0, 16.0, 9.0),
            BooleanOp.OR
    );
    protected static final VoxelShape UP_WITH_SMALL_AABB = Shapes.join(
            UP_AABB,
            SMALL_COIL_AABB,
            BooleanOp.OR
    );
    protected static final VoxelShape UP_WITH_FULL_AABB = Shapes.join(
            UP_AABB,
            FULL_COIL_AABB,
            BooleanOp.OR
    );
    protected static final VoxelShape DOWN_AABB = Shapes.join(
            Block.box(5.0, 13.0, 5.0, 11.0, 16.0, 11.0),
            Block.box(7.0, 0.0, 7.0, 9.0, 13.0, 9.0),
            BooleanOp.OR
    );
    protected static final VoxelShape DOWN_WITH_SMALL_AABB = Shapes.join(
            DOWN_AABB,
            SMALL_COIL_AABB,
            BooleanOp.OR
    );
    protected static final VoxelShape DOWN_WITH_FULL_AABB = Shapes.join(
            DOWN_AABB,
            FULL_COIL_AABB,
            BooleanOp.OR
    );
    protected static final VoxelShape EAST_AABB = Block.box(0.0, 2.0, 6.0, 13.0, 15.0, 10.0);
    protected static final VoxelShape WEST_AABB = Block.box(3.0, 2.0, 6.0, 16.0, 15.0, 10.0);
    protected static final VoxelShape NORTH_AABB = Block.box(6.0, 2.0, 3.0, 10.0, 15.0, 16.0);
    protected static final VoxelShape SOUTH_AABB = Block.box(6.0, 2.0, 0.0, 10.0, 15.0, 13.0);

    @Override
    public MapCodec<HyphalConductorBlock> codec() {
        return CODEC;
    }

    public HyphalConductorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.UP).setValue(STICKY, false).setValue(CONDUCTOR_STATE, ConductorStateProperty.ConductorState.EMPTY)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(STICKY);
        builder.add(CONDUCTOR_STATE);
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        CustomData data = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
        if (data.contains(HyphalConductorBlockEntity.TAG_WIRE_ITEM)) {
            level.setBlock(pos, state.setValue(CONDUCTOR_STATE, ConductorStateProperty.ConductorState.HOLDING_WIRE), 2);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof HyphalConductorBlockEntity blockEntity) {
                blockEntity.popOutTheItem();
                blockEntity.unlink();
            }

            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = this.defaultBlockState().setValue(FACING, context.getClickedFace());
        if(state.canSurvive(context.getLevel(), context.getClickedPos())) {
            return state;
        } else {
            return null;
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.getValue(FACING).getOpposite() == direction && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facingDirection = state.getValue(FACING);
        Direction attachedDirection = facingDirection.getOpposite();
        BlockPos attachPos = pos.relative(attachedDirection);
        return level.getBlockState(attachPos).isFaceSturdy(level, attachPos, facingDirection);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        Direction facing = state.getValue(FACING);
        ConductorStateProperty.ConductorState conductorState = state.getValue(CONDUCTOR_STATE);
        return switch (facing) {
            case NORTH -> NORTH_AABB;
            case SOUTH -> SOUTH_AABB;
            case EAST -> EAST_AABB;
            case WEST -> WEST_AABB;
            case DOWN -> switch(conductorState) {
                case EMPTY -> DOWN_AABB;
                case HOLDING_WIRE -> DOWN_WITH_FULL_AABB;
                case ACCEPTING_WIRE -> DOWN_WITH_SMALL_AABB;
            };
            case UP -> switch(conductorState) {
                case EMPTY -> UP_AABB;
                case HOLDING_WIRE -> UP_WITH_FULL_AABB;
                case ACCEPTING_WIRE -> UP_WITH_SMALL_AABB;
            };
        };
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HyphalConductorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide
                ? null
                : createTickerHelper(blockEntityType, YWSaNFBlockEntityTypes.HYPHAL_CONDUCTOR, HyphalConductorBlockEntity::serverTick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemInteractionResult success = ItemInteractionResult.sidedSuccess(level.isClientSide);

        boolean sticky = state.getValue(STICKY);
        ConductorStateProperty.ConductorState conductorState = state.getValue(CONDUCTOR_STATE);

        if(player.getAbilities().mayBuild) {
            if (!sticky && stack.is(Items.SLIME_BALL)) {
                // add sticky
                if (!level.isClientSide) {
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, pos, stack);
                    }

                    BlockState newState = state.setValue(STICKY, true);
                    level.setBlock(pos, newState, 11);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));

                    stack.consume(1, player);
                }
                level.playSound(player, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
                return success;
            } else if (sticky && stack.is(ItemTags.AXES)) {
                // remove sticky
                if (!level.isClientSide) {
                    if (player instanceof ServerPlayer) {
                        CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer) player, pos, stack);
                    }

                    BlockState newState = state.setValue(STICKY, false);
                    level.setBlock(pos, newState, 11);
                    level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));

                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                }
                level.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.levelEvent(player, LevelEvent.PARTICLES_WAX_OFF, pos, 0);
                return success;
            }
        }

        if(level.getBlockEntity(pos) instanceof HyphalConductorBlockEntity blockEntity) {
            if(blockEntity.updateBlockStateIfNeeded()) {
                return success;
            }

            switch (conductorState) {
                case HOLDING_WIRE -> {
                    if (!sticky && tryTakeWireFromSource(level, pos, blockEntity, player)) {
                        return success;
                    } else if (tryReturnWireToSource(level, pos, blockEntity, player)) {
                        return success;
                    }
                }
                case ACCEPTING_WIRE -> {
                    if (!sticky && tryTakeWireFromEndpoint(level, pos, blockEntity, player)) {
                        return success;
                    }
                }
                case EMPTY -> {
                    if (player.getAbilities().mayBuild && tryInsertWire(level, pos, state, blockEntity, stack, player)) {
                        return success;
                    } else if (tryConnectWireToEndpoint(level, pos, blockEntity, player)) {
                        return success;
                    }
                }
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public static boolean tryTakeWireFromSource(Level level, BlockPos pos, HyphalConductorBlockEntity blockEntity, Entity entity) {
        if(!blockEntity.canLinkTo(entity)) {
            return false;
        }

        if(blockEntity.getLinkedEntity() == null && blockEntity.getLinkedBlockPos() == null) {
            if(!level.isClientSide) {
                blockEntity.linkTo(entity);

                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return true;
        }
        return false;
    }

    public static boolean tryReturnWireToSource(Level level, BlockPos pos, HyphalConductorBlockEntity blockEntity, Entity entity) {
        if(blockEntity.getLinkedEntity() == entity) {
            if (!level.isClientSide) {
                blockEntity.unlink();
                blockEntity.sendUpdate();

                level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return true;
        }
        return false;
    }

    public static boolean tryTakeWireFromEndpoint(Level level, BlockPos pos, HyphalConductorBlockEntity blockEntity, Entity entity) {
        BlockPos linkedPos = blockEntity.getLinkedBlockPos();
        if(linkedPos != null) {
            if(level.getBlockEntity(linkedPos) instanceof HyphalConductorBlockEntity linkedBlockEntity) {
                if(linkedBlockEntity.canLinkTo(entity)) {
                    if(!level.isClientSide) {
                        linkedBlockEntity.linkTo(entity);

                        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean tryConnectWireToEndpoint(Level level, BlockPos pos, HyphalConductorBlockEntity blockEntity, Entity entity) {
        HyphalConductorBlockEntity linkedBlockEntity = YWSaNFEntityAttachment.getAttachment(entity).getFirstLink();
        if(linkedBlockEntity == null) {
            return false;
        }

        if(!linkedBlockEntity.canLinkTo(blockEntity)) {
            return false;
        }

        if (!level.isClientSide) {
            linkedBlockEntity.linkTo(pos);

            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return true;
    }

    public static boolean tryInsertWire(Level level, BlockPos pos, BlockState state, HyphalConductorBlockEntity blockEntity, ItemStack stack, Player player) {
        if (!canAcceptStack(stack)) {
            return false;
        }

        if (!level.isClientSide) {
            ItemStack itemstack = stack.consumeAndReturn(1, player);
            blockEntity.setTheItem(itemstack);
            blockEntity.checkBlockStateAndSendUpdate();

            if(blockEntity.canLinkTo(player) && state.hasProperty(STICKY) && !state.getValue(STICKY)) {
                blockEntity.linkTo(player);
            }

            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.ITEM_FRAME_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return true;
    }

    public static boolean canAcceptStack(ItemStack stack) {
        return stack.has(YWSaNFComponentTypes.WIRE_LINE_COMPONENT);
    }
}
