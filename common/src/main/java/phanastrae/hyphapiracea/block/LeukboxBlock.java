package phanastrae.hyphapiracea.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
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
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.block.entity.HyphaPiraceaBlockEntityTypes;
import phanastrae.hyphapiracea.block.entity.LeukboxBlockEntity;
import phanastrae.hyphapiracea.block.state.HyphaPiraceaBlockProperties;
import phanastrae.hyphapiracea.component.HyphaPiraceaComponentTypes;
import phanastrae.hyphapiracea.component.type.DiscLockComponent;
import phanastrae.hyphapiracea.item.HyphaPiraceaItems;
import phanastrae.hyphapiracea.item.LeukboxLockItem;
import phanastrae.hyphapiracea.structure.leubox_stages.AbstractLeukboxStage;

public class LeukboxBlock extends BaseEntityBlock implements MiniCircuitHolder {
    public static final MapCodec<LeukboxBlock> CODEC = simpleCodec(LeukboxBlock::new);
    public static final BooleanProperty HAS_DISC = HyphaPiraceaBlockProperties.HAS_DISC;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    @Override
    public MapCodec<LeukboxBlock> codec() {
        return CODEC;
    }

    public LeukboxBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(HAS_DISC, false)
                .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_DISC, FACING);
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
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LeukboxBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide
                ? createTickerHelper(blockEntityType, HyphaPiraceaBlockEntityTypes.PIRACEATIC_LEUKBOX, LeukboxBlockEntity::clientTick)
                : createTickerHelper(blockEntityType, HyphaPiraceaBlockEntityTypes.PIRACEATIC_LEUKBOX, LeukboxBlockEntity::serverTick);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        CustomData data = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
        if (data.contains(LeukboxBlockEntity.TAG_DISC_ITEM)) {
            level.setBlock(pos, state.setValue(HAS_DISC, true), 2);
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos) {
        if(level.getBlockEntity(pos) instanceof LeukboxBlockEntity blockEntity) {
            return blockEntity.getStage().getComparatorValue();
        } else {
            return 1;
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof LeukboxBlockEntity blockEntity) {
                blockEntity.popOutTheItem();
            }

            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(stack.is(HyphaPiraceaItems.LEUKBOX_LOCK) && player.getAbilities().mayBuild) {
            if(level.getBlockEntity(pos) instanceof LeukboxBlockEntity leukboxBlockEntity) {
                String lockLock = DiscLockComponent.getDiscLockFromLock(stack);
                String boxLock = leukboxBlockEntity.getLeukboxLock();

                if(!lockLock.equals(boxLock)) {
                    LeukboxLockItem.playLockSound(player);
                    if(!level.isClientSide()) {
                        leukboxBlockEntity.setLeukboxLock(lockLock);
                        Component component = lockLock.isEmpty()
                                ? Component.translatable("hyphapiracea.leukbox.lock.set.empty")
                                : Component.translatable("hyphapiracea.leukbox.lock.set", lockLock);

                        player.displayClientMessage(component, true);
                    }

                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }

        if (state.getValue(HAS_DISC)) {
            ItemInteractionResult iteminteractionresult = tryRemoveItem(level, pos, player);
            return !iteminteractionresult.consumesAction() ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : iteminteractionresult;
        } else {
            ItemStack itemstack = player.getItemInHand(hand);
            ItemInteractionResult iteminteractionresult = tryInsertItem(level, pos, itemstack, player);
            return !iteminteractionresult.consumesAction() ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : iteminteractionresult;
        }
    }

    public static ItemInteractionResult tryRemoveItem(Level level, BlockPos pos, Player player) {
        BlockState blockstate = level.getBlockState(pos);
        if (blockstate.is(HyphaPiraceaBlocks.PIRACEATIC_LEUKBOX) && blockstate.getValue(HAS_DISC)) {
            if (!level.isClientSide) {
                if (level.getBlockEntity(pos) instanceof LeukboxBlockEntity blockEntity) {
                    if(blockEntity.discIsRecoverable() || blockEntity.getStage() == AbstractLeukboxStage.LeukboxStage.ERROR) {
                        blockEntity.popOutTheItem();
                        blockEntity.setDiscRecoverable(true);

                        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.SCAFFOLDING_BREAK, SoundSource.BLOCKS, 1.5F, 0.2F);
                        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, blockstate));
                    }
                }
            }

            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
    }

    public static ItemInteractionResult tryInsertItem(Level level, BlockPos pos, ItemStack stack, Player player) {
        if (!stack.has(HyphaPiraceaComponentTypes.KEYED_DISC_COMPONENT)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else {
            BlockState blockstate = level.getBlockState(pos);
            if (blockstate.is(HyphaPiraceaBlocks.PIRACEATIC_LEUKBOX) && !blockstate.getValue(HAS_DISC)) {
                if (level.getBlockEntity(pos) instanceof LeukboxBlockEntity blockEntity) {
                    String discLock = DiscLockComponent.getDiscLockFromDisc(stack);
                    String boxLock = blockEntity.getLeukboxLock();
                    if(discLock.equals(boxLock)) {
                        if (!level.isClientSide) {
                            ItemStack itemstack = stack.consumeAndReturn(1, player);

                            blockEntity.setTheItem(itemstack);
                            blockEntity.setDiscRecoverable(true);

                            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.5F, 0.2F);
                            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, blockstate));
                        }

                        return ItemInteractionResult.sidedSuccess(level.isClientSide);
                    } else {
                        MutableComponent component = boxLock.isEmpty()
                                ? Component.translatable("hyphapiracea.leukbox.lock.invalid.empty")
                                : Component.translatable("hyphapiracea.leukbox.lock.invalid", boxLock);
                        player.displayClientMessage(component.withStyle(ChatFormatting.RED), true);

                        return ItemInteractionResult.CONSUME;
                    }
                }
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    @Nullable
    public MiniCircuit getMiniCircuit(BlockGetter blockGetter, BlockPos pos, BlockState state, Direction side) {
        if(blockGetter.getBlockEntity(pos) instanceof MiniCircuitHolder mch) {
            return mch.getMiniCircuit(blockGetter, pos, state, side);
        } else {
            return null;
        }
    }
}
