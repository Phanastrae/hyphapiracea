package phanastrae.hyphapiracea.client.renderer.block;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import phanastrae.hyphapiracea.block.HyphaPiraceaBlocks;
import phanastrae.hyphapiracea.client.services.XPlatClientInterface;

public class HyphaPiraceaBlockRenderLayers {

    public static void init() {
        putBlocks(RenderType.cutoutMipped(),
                HyphaPiraceaBlocks.AZIMULDEY_MASS,
                HyphaPiraceaBlocks.HYPHAL_NODE,
                HyphaPiraceaBlocks.STORMSAP_CELL);
        putBlocks(RenderType.translucent(),
                HyphaPiraceaBlocks.PIRACEATIC_TAR,
                HyphaPiraceaBlocks.PIRACEATIC_GLOBGLASS);
    }

    private static void putBlocks(RenderType renderLayer, Block... blocks) {
        XPlatClientInterface.INSTANCE.registerBlockRenderLayers(renderLayer, blocks);
    }
}
