package phanastrae.ywsanf.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import phanastrae.ywsanf.client.YWSaNFClient;
import phanastrae.ywsanf.client.particle.YWSaNFParticles;
import phanastrae.ywsanf.client.renderer.entity.model.YWSaNFEntityModelLayers;

public class YWSaNFClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        YWSaNFClient.init();

        // entity model layers
        YWSaNFEntityModelLayers.init(((modelLayerLocation, layerDefinitionSupplier) -> EntityModelLayerRegistry.registerModelLayer(modelLayerLocation, layerDefinitionSupplier::get)));

        // particles
        YWSaNFParticles.init(new YWSaNFParticles.ClientParticleRegistrar() {
            @Override
            public <T extends ParticleOptions> void register(ParticleType<T> type, YWSaNFParticles.ParticleRegistration<T> registration) {
                ParticleFactoryRegistry.getInstance().register(type, registration::create);
            }
        });
    }
}
