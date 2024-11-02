package phanastrae.hyphapiracea.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.block.entity.LeukboxBlockEntity;
import phanastrae.hyphapiracea.block.entity.HyphaPiraceaBlockEntityTypes;
import phanastrae.hyphapiracea.block.state.HyphaPiraceaBlockProperties;
import phanastrae.hyphapiracea.item.HyphaPiraceaItems;

public class LeukboxBlock extends BaseEntityBlock {
    public static final MapCodec<LeukboxBlock> CODEC = simpleCodec(LeukboxBlock::new);
    public static final BooleanProperty HAS_DISC = HyphaPiraceaBlockProperties.HAS_DISC;

    @Override
    public MapCodec<LeukboxBlock> codec() {
        return CODEC;
    }

    public LeukboxBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_DISC, Boolean.valueOf(false)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_DISC);
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
                ? createTickerHelper(blockEntityType, HyphaPiraceaBlockEntityTypes.LEUKBOX, LeukboxBlockEntity::clientTick)
                : createTickerHelper(blockEntityType, HyphaPiraceaBlockEntityTypes.LEUKBOX, LeukboxBlockEntity::serverTick);
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
        if (state.getValue(HAS_DISC)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else {
            ItemStack itemstack = player.getItemInHand(hand);
            ItemInteractionResult iteminteractionresult = tryInsertItem(level, pos, itemstack, player);
            return !iteminteractionresult.consumesAction() ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : iteminteractionresult;
        }
    }

    public static ItemInteractionResult tryInsertItem(Level level, BlockPos pos, ItemStack stack, Player player) {
        if (!stack.is(HyphaPiraceaItems.KEYED_DISC)) { // TODO tweak criteria
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else {
            BlockState blockstate = level.getBlockState(pos);
            if (blockstate.is(HyphaPiraceaBlocks.LEUKBOX) && !blockstate.getValue(HAS_DISC)) {
                if (!level.isClientSide) {
                    ItemStack itemstack = stack.consumeAndReturn(1, player);
                    if (level.getBlockEntity(pos) instanceof LeukboxBlockEntity blockEntity) {
                        blockEntity.setTheItem(itemstack);
                        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.5F, 0.2F);
                        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, blockstate));
                    }
                }

                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            } else {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
        }
    }
}
