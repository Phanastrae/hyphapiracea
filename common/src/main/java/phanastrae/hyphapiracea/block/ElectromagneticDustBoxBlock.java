package phanastrae.hyphapiracea.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.Vec3;
import phanastrae.hyphapiracea.particle.HyphaPiraceaParticleTypes;

public class ElectromagneticDustBoxBlock extends Block {
    public static final MapCodec<ElectromagneticDustBoxBlock> CODEC = simpleCodec(ElectromagneticDustBoxBlock::new);
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    @Override
    public MapCodec<ElectromagneticDustBoxBlock> codec() {
        return CODEC;
    }

    public ElectromagneticDustBoxBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(POWERED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(POWERED);
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite()).setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            if(!level.isClientSide) {
                level.blockEvent(pos, this, 0, 0);
            }

            level.setBlock(pos, state.setValue(POWERED, powered), 3);
        }
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        spawnParticles(level, pos, state, 40, level.getRandom());
        return true;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        spawnParticles(level, pos, state, 2 + random.nextInt(4), random);
    }

    public void spawnParticles(Level level, BlockPos pos, BlockState state, int count, RandomSource random) {
        if(state.getValue(POWERED)) {
            Direction direction = state.getValue(FACING);
            Vec3i normal = direction.getNormal();
            Vec3 p = pos.getCenter().add(normal.getX() * 0.5, normal.getY() * 0.5, normal.getZ() * 0.5);
            for (int i = 0; i < count; i++) {
                level.addParticle(
                        HyphaPiraceaParticleTypes.ELECTROMAGNETIC_DUST,
                        p.x + (normal.getX() == 0 ? (random.nextFloat() * 2 - 1) * 0.375 : 0),
                        p.y + (normal.getY() == 0 ? (random.nextFloat() * 2 - 1) * 0.375 : 0),
                        p.z + (normal.getZ() == 0 ? (random.nextFloat() * 2 - 1) * 0.375 : 0),
                        normal.getX() * 0.15 + (random.nextFloat() * 2 - 1) * 0.05,
                        normal.getY() * 0.15 + (random.nextFloat() * 2 - 1) * 0.05,
                        normal.getZ() * 0.15 + (random.nextFloat() * 2 - 1) * 0.05
                );
            }
        }
    }
}
