package phanastrae.ywsanf.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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
}
