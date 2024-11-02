package phanastrae.hyphapiracea.client.renderer.block.entity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import phanastrae.hyphapiracea.block.entity.HyphaPiraceaBlockEntityTypes;
import phanastrae.hyphapiracea.mixin.client.BlockEntityRenderersAccessor;

public class HyphaPiraceaBlockEntityRenderers {

    public static void init() {
        register(HyphaPiraceaBlockEntityTypes.PIRACEATIC_LEUKBOX, LeukboxBlockEntityRenderer::new);
        register(HyphaPiraceaBlockEntityTypes.HYPHAL_CONDUCTOR, HyphalConductorBlockEntityRenderer::new);
        register(HyphaPiraceaBlockEntityTypes.HYPHAL_NODE, HyphalNodeBlockEntityRenderer::new);
        register(HyphaPiraceaBlockEntityTypes.HYPHAL_AMMETER, AmmeterBlockEntityRenderer::new);
        register(HyphaPiraceaBlockEntityTypes.HYPHAL_VOLTMETER, VoltmeterBlockEntityRenderer::new);
        register(HyphaPiraceaBlockEntityTypes.STORMSAP_CELL, StormsapCellBlockEntityRenderer::new);
    }

    public static <T extends BlockEntity> void register(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> factory) {
        BlockEntityRenderersAccessor.invokeRegister(type, factory);
    }
}
