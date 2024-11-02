package phanastrae.ywsanf.client.renderer.block;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import phanastrae.ywsanf.block.YWSaNFBlocks;
import phanastrae.ywsanf.client.services.XPlatClientInterface;

public class YWSaNFBlockRenderLayers {

    public static void init() {
        putBlocks(RenderType.cutoutMipped(),
                YWSaNFBlocks.STORMSAP_CELL);
        putBlocks(RenderType.translucent(),
                YWSaNFBlocks.FEASTING_TAR);
    }

    private static void putBlocks(RenderType renderLayer, Block... blocks) {
        XPlatClientInterface.INSTANCE.registerBlockRenderLayers(renderLayer, blocks);
    }
}
