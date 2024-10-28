package phanastrae.ywsanf.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.block.entity.MagnetometerBlockEntity;
import phanastrae.ywsanf.block.entity.YWSaNFBlockEntityTypes;
import phanastrae.ywsanf.item.MagnetometerItem;

public class MagnetometerBlock extends BaseEntityBlock {
    public static final MapCodec<MagnetometerBlock> CODEC = simpleCodec(MagnetometerBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    @Override
    public MapCodec<MagnetometerBlock> codec() {
        return CODEC;
    }

    public MagnetometerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(POWERED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean flag = level.hasNeighborSignal(pos);
        if (flag != state.getValue(POWERED)) {
            if (flag) {
                this.spawnFieldLine(null, level, pos);
            }

            level.setBlock(pos, state.setValue(POWERED, flag), 3);
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MagnetometerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, YWSaNFBlockEntityTypes.MAGNETOMETER_BLOCK, MagnetometerBlockEntity::serverTick);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if(level.getBlockEntity(pos) instanceof MagnetometerBlockEntity blockEntity) {
            return blockEntity.getComparatorOutput();
        } else {
            return 0;
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult
    ) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        } else {
            this.spawnFieldLine(player, level, pos);
            return ItemInteractionResult.CONSUME;
        }
    }

    private void spawnFieldLine(@Nullable Entity entity, Level level, BlockPos pos) {
        level.blockEvent(pos, this, 0, 0);
        level.gameEvent(entity, GameEvent.BLOCK_ACTIVATE, pos);
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        Vec3 p = pos.getCenter();

        RandomSource randomSource = level.getRandom();
        level.playSound(null, p.x, p.y, p.z, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 1.0F, 1.7F + randomSource.nextFloat() * 0.2F);
        level.playSound(null, p.x, p.y, p.z, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 1.0F, 1.4F + randomSource.nextFloat() * 0.3F);

        MagnetometerItem.spawnFieldLine(level, p, 240, 0.12);

        return true;
    }
}
