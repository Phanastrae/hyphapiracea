package phanastrae.hyphapiracea.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.block.entity.StormsapCellBlockEntity;
import phanastrae.hyphapiracea.block.entity.HyphaPiraceaBlockEntityTypes;
import phanastrae.hyphapiracea.block.state.HyphaPiraceaBlockProperties;

public class StormsapCellBlock extends AbstractTwoSidedChargeSacBlock {
    public static final MapCodec<StormsapCellBlock> CODEC = simpleCodec(StormsapCellBlock::new);
    public static final BooleanProperty ALWAYS_SHOW_INFO = HyphaPiraceaBlockProperties.ALWAYS_SHOW_INFO;
    public static final IntegerProperty STORED_POWER = HyphaPiraceaBlockProperties.STORED_POWER;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    @Override
    public MapCodec<StormsapCellBlock> codec() {
        return CODEC;
    }

    public StormsapCellBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(ALWAYS_SHOW_INFO, false)
                        .setValue(STORED_POWER, 0)
                        .setValue(POWERED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ALWAYS_SHOW_INFO, STORED_POWER, POWERED);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StormsapCellBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide
                ? null
                : createTickerHelper(blockEntityType, HyphaPiraceaBlockEntityTypes.STORMSAP_CELL, StormsapCellBlockEntity::serverTick);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if(level.getBlockEntity(pos) instanceof StormsapCellBlockEntity blockEntity) {
            return blockEntity.getComparatorOutput();
        } else {
            return 0;
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            if(level.getBlockEntity(pos) instanceof StormsapCellBlockEntity blockEntity) {
                blockEntity.setPowered(powered);
            }

            level.setBlock(pos, state.setValue(POWERED, powered), 3);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(stack.is(Items.REDSTONE)) {
            if (level.getBlockEntity(pos) instanceof StormsapCellBlockEntity blockEntity) {
                if (!level.isClientSide) {
                    stack.consume(1, player);
                    blockEntity.addEnergy(100000);
                    blockEntity.sendUpdate();

                    level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        if(player.getAbilities().mayBuild) {
            if(!level.isClientSide) {
                level.setBlock(pos, state.setValue(ALWAYS_SHOW_INFO, !state.getValue(ALWAYS_SHOW_INFO)), 3);
            }
            level.playSound(player, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
