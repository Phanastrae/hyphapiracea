package phanastrae.ywsanf.client.services;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import phanastrae.ywsanf.services.Services;

public interface XPlatClientInterface {
    XPlatClientInterface INSTANCE = Services.load(XPlatClientInterface.class);

    void registerBlockRenderLayers(RenderType renderLayer, Block... blocks);
}
