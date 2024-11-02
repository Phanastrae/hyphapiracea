package phanastrae.hyphapiracea.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FeastingTarBlock extends TransparentBlock {
    public static final MapCodec<FeastingTarBlock> CODEC = simpleCodec(FeastingTarBlock::new);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;

    @Override
    public MapCodec<FeastingTarBlock> codec() {
        return CODEC;
    }

    public FeastingTarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(AGE, 0)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if(state.getValue(AGE) == 0 && random.nextInt(10) == 0) {
            level.setBlockAndUpdate(pos, state.cycle(AGE));
            level.scheduleTick(pos, this, 20 + random.nextInt(10));
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if(state.getValue(AGE) < 7) {
            level.setBlockAndUpdate(pos, state.cycle(AGE));
            level.scheduleTick(pos, this, 20 + random.nextInt(10));
        } else {
            level.destroyBlock(pos, true);
            for(int i = 0; i < 20; i++) {
                BlockPos adjPos;
                if(i <= 5) {
                    adjPos = pos.offset(Direction.from3DDataValue(i).getNormal());
                } else {
                    adjPos = pos.offset(random.nextInt(7) - 3, random.nextInt(7) - 3, random.nextInt(7) - 3);
                }
                BlockState adjState = level.getBlockState(adjPos);
                Block adjBlock = adjState.getBlock();
                if(adjBlock == this) {
                    if(adjState.hasProperty(AGE)) {
                        if(adjState.getValue(AGE) < 5) {
                            level.setBlockAndUpdate(adjPos, adjState.setValue(AGE, 5));
                        }
                    }
                    level.scheduleTick(adjPos, adjBlock, 5 + random.nextInt(3));
                }
            }
        }
    }
}
