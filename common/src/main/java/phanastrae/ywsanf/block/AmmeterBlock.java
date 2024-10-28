package phanastrae.ywsanf.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.block.entity.AmmeterBlockEntity;
import phanastrae.ywsanf.block.entity.YWSaNFBlockEntityTypes;

public class AmmeterBlock extends AbstractTwoSidedChargeSacBlock {
    public static final MapCodec<AmmeterBlock> CODEC = simpleCodec(AmmeterBlock::new);

    @Override
    public MapCodec<AmmeterBlock> codec() {
        return CODEC;
    }

    public AmmeterBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AmmeterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide
                ? null
                : createTickerHelper(blockEntityType, YWSaNFBlockEntityTypes.AMMETER_BLOCK, AmmeterBlockEntity::serverTick);
    }
}
