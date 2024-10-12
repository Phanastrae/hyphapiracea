package phanastrae.ywsanf.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.block.entity.LeukboxBlockEntity;
import phanastrae.ywsanf.block.entity.YWSaNFBlockEntityTypes;
import phanastrae.ywsanf.item.YWSaNFItems;

public class LeukboxBlock extends BaseEntityBlock {
    public static final MapCodec<LeukboxBlock> CODEC = simpleCodec(LeukboxBlock::new);

    @Override
    public MapCodec<LeukboxBlock> codec() {
        return CODEC;
    }

    public LeukboxBlock(Properties properties) {
        super(properties);
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
        return level.isClientSide ? null : createTickerHelper(blockEntityType, YWSaNFBlockEntityTypes.LEUKBOX, LeukboxBlockEntity::serverTick);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack heldStack = player.getItemInHand(hand);
        if(heldStack.is(YWSaNFItems.KEYED_DISC)) {
            if(!level.isClientSide && level instanceof ServerLevel serverLevel) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if(blockEntity instanceof LeukboxBlockEntity leukboxBlockEntity) {
                    // TODO important: remove this rename-based system, this is just here for easy testing
                    if(heldStack.has(DataComponents.CUSTOM_NAME)) {
                        Component component = heldStack.get(DataComponents.CUSTOM_NAME);
                        if(component != null) {
                            String s = component.getString();
                            ResourceLocation rl = ResourceLocation.tryParse(s);
                            if(rl != null) {
                                leukboxBlockEntity.startGeneratingStructure(rl, pos, serverLevel);
                            }
                        }
                    }
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
    }
}
