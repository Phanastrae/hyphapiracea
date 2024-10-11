package phanastrae.ywsanf.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import phanastrae.ywsanf.YWSaNF;

import java.util.function.BiConsumer;

public class YWSaNFBlocks {

    public static final Block LEUKBOX = new LeukboxBlock(properties());

    public static void init(BiConsumer<ResourceLocation, Block> r) {
        r.accept(id("leukbox"), LEUKBOX);
    }

    private static ResourceLocation id(String path) {
        return YWSaNF.id(path);
    }

    private static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.of();
    }
}
