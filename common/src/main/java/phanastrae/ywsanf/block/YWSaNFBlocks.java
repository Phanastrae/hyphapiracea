package phanastrae.ywsanf.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import phanastrae.ywsanf.YWSaNF;

import java.util.function.BiConsumer;

public class YWSaNFBlocks {

    public static final Block LEUKBOX = new LeukboxBlock(properties());
    public static final Block HYPHAL_CONDUCTOR = new HyphalConductorBlock(properties());
    public static final Block FEASTING_TAR = new Block(properties().emissiveRendering((state, blockGetter, blockPos) -> true));
    public static final Block MAGNETOMETER_BLOCK = new MagnetometerBlock(properties());

    public static void init(BiConsumer<ResourceLocation, Block> r) {
        r.accept(id("leukbox"), LEUKBOX);
        r.accept(id("hyphal_conductor"), HYPHAL_CONDUCTOR);
        r.accept(id("feasting_tar"), FEASTING_TAR);
        r.accept(id("magnetometer_block"), MAGNETOMETER_BLOCK);
    }

    private static ResourceLocation id(String path) {
        return YWSaNF.id(path);
    }

    private static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.of();
    }
}
