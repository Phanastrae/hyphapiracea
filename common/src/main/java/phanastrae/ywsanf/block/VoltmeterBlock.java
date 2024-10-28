package phanastrae.ywsanf.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.block.entity.VoltmeterBlockEntity;
import phanastrae.ywsanf.block.entity.YWSaNFBlockEntityTypes;

public class VoltmeterBlock extends AbstractTwoSidedChargeSacBlock {
    public static final MapCodec<VoltmeterBlock> CODEC = simpleCodec(VoltmeterBlock::new);

    @Override
    public MapCodec<VoltmeterBlock> codec() {
        return CODEC;
    }

    public VoltmeterBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VoltmeterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide
                ? null
                : createTickerHelper(blockEntityType, YWSaNFBlockEntityTypes.VOLTMETER_BLOCK, VoltmeterBlockEntity::serverTick);
    }
}
