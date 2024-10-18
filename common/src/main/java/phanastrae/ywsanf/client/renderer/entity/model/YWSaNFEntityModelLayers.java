package phanastrae.ywsanf.client.renderer.entity.model;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.client.renderer.block.entity.HyphalConductorBlockEntityRenderer;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class YWSaNFEntityModelLayers {

    public static final ModelLayerLocation HYPHALINE_COIL = createMainLayer("hyphaline_coil");
    public static final ModelLayerLocation HYPHALINE_COIL_SMALL = createMainLayer("hyphaline_coil_small");

    public static void init(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> r) {
        r.accept(HYPHALINE_COIL, HyphalConductorBlockEntityRenderer::createTallCoilBodyLayer);
        r.accept(HYPHALINE_COIL_SMALL, HyphalConductorBlockEntityRenderer::createSmallCoilBodyLayer);
    }

    private static ModelLayerLocation createMainLayer(String id) {
        return new ModelLayerLocation(YWSaNF.id(id), "main");
    }

    private static ModelLayerLocation createLayer(String id, String layer) {
        return new ModelLayerLocation(YWSaNF.id(id), layer);
    }
}
