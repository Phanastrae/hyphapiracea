package phanastrae.hyphapiracea.client.services;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import phanastrae.hyphapiracea.services.Services;

public interface XPlatClientInterface {
    XPlatClientInterface INSTANCE = Services.load(XPlatClientInterface.class);

    void registerBlockRenderLayers(RenderType renderLayer, Block... blocks);
}
