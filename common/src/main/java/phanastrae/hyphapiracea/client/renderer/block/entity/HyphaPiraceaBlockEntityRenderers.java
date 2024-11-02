package phanastrae.hyphapiracea.client.renderer.block.entity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import phanastrae.hyphapiracea.block.entity.HyphaPiraceaBlockEntityTypes;
import phanastrae.hyphapiracea.mixin.client.BlockEntityRenderersAccessor;

public class HyphaPiraceaBlockEntityRenderers {

    public static void init() {
        register(HyphaPiraceaBlockEntityTypes.LEUKBOX, LeukboxBlockEntityRenderer::new);
        register(HyphaPiraceaBlockEntityTypes.HYPHAL_CONDUCTOR, HyphalConductorBlockEntityRenderer::new);
        register(HyphaPiraceaBlockEntityTypes.GALVANOCARPIC_BULB, GalvanocarpicBulbBlockEntityRenderer::new);
        register(HyphaPiraceaBlockEntityTypes.AMMETER_BLOCK, AmmeterBlockEntityRenderer::new);
        register(HyphaPiraceaBlockEntityTypes.VOLTMETER_BLOCK, VoltmeterBlockEntityRenderer::new);
        register(HyphaPiraceaBlockEntityTypes.STORMSAP_CELL, StormsapCellBlockEntityRenderer::new);
    }

    public static <T extends BlockEntity> void register(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> factory) {
        BlockEntityRenderersAccessor.invokeRegister(type, factory);
    }
}
