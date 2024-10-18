package phanastrae.ywsanf.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import phanastrae.ywsanf.client.YWSaNFClient;
import phanastrae.ywsanf.client.renderer.entity.model.YWSaNFEntityModelLayers;

public class YWSaNFClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        YWSaNFClient.init();

        // entity model layers
        YWSaNFEntityModelLayers.init(((modelLayerLocation, layerDefinitionSupplier) -> EntityModelLayerRegistry.registerModelLayer(modelLayerLocation, layerDefinitionSupplier::get)));
    }
}
