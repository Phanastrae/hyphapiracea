package phanastrae.ywsanf.client.renderer.block.entity;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import phanastrae.ywsanf.block.entity.YWSaNFBlockEntityTypes;
import phanastrae.ywsanf.mixin.client.BlockEntityRenderersAccessor;

public class YWSaNFBlockEntityRenderers {

    public static void init() {
        register(YWSaNFBlockEntityTypes.LEUKBOX, LeukboxBlockEntityRenderer::new);
        register(YWSaNFBlockEntityTypes.HYPHAL_CONDUCTOR, HyphalConductorBlockEntityRenderer::new);
    }

    public static <T extends BlockEntity> void register(BlockEntityType<? extends T> type, BlockEntityRendererProvider<T> factory) {
        BlockEntityRenderersAccessor.invokeRegister(type, factory);
    }
}
