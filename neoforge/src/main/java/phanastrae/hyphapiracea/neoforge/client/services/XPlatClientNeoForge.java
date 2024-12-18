package phanastrae.hyphapiracea.neoforge.client.services;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import phanastrae.hyphapiracea.client.services.XPlatClientInterface;

public class XPlatClientNeoForge implements XPlatClientInterface {

    @Override
    public void registerBlockRenderLayers(RenderType renderLayer, Block... blocks) {
        for(Block block : blocks) {
            ItemBlockRenderTypes.setRenderLayer(block, renderLayer);
        }
    }
}
