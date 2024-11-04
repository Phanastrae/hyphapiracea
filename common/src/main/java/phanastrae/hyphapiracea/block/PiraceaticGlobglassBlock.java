package phanastrae.hyphapiracea.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PiraceaticGlobglassBlock extends TransparentBlock {
    public static final MapCodec<PiraceaticGlobglassBlock> CODEC = simpleCodec(PiraceaticGlobglassBlock::new);

    @Override
    public MapCodec<PiraceaticGlobglassBlock> codec() {
        return CODEC;
    }

    public PiraceaticGlobglassBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
