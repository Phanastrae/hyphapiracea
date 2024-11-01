package phanastrae.ywsanf.block;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.block.entity.StormsapCellBlockEntity;
import phanastrae.ywsanf.block.entity.YWSaNFBlockEntityTypes;

public class StormsapCellBlock extends AbstractTwoSidedChargeSacBlock {
    public static final MapCodec<StormsapCellBlock> CODEC = simpleCodec(StormsapCellBlock::new);

    @Override
    public MapCodec<StormsapCellBlock> codec() {
        return CODEC;
    }

    public StormsapCellBlock(Properties properties) {
        super(properties);
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
                : createTickerHelper(blockEntityType, YWSaNFBlockEntityTypes.STORMSAP_CELL, StormsapCellBlockEntity::serverTick);
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


        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
