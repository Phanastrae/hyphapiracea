package phanastrae.hyphapiracea.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import phanastrae.hyphapiracea.client.HyphaPiraceaClient;
import phanastrae.hyphapiracea.client.particle.HyphaPiraceaParticles;
import phanastrae.hyphapiracea.client.renderer.entity.HyphaPiraceaEntityRenderers;
import phanastrae.hyphapiracea.client.renderer.entity.model.HyphaPiraceaEntityModelLayers;

public class HyphaPiraceaClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HyphaPiraceaClient.init();

        // block color handlers
        HyphaPiraceaClient.registerBlockColorHandlers(ColorProviderRegistry.BLOCK::register);

        // entity renderers
        HyphaPiraceaEntityRenderers.init(this::registerEntityRenderer);

        // entity model layers
        HyphaPiraceaEntityModelLayers.init(((modelLayerLocation, layerDefinitionSupplier) -> EntityModelLayerRegistry.registerModelLayer(modelLayerLocation, layerDefinitionSupplier::get)));

        // particles
        HyphaPiraceaParticles.init(new HyphaPiraceaParticles.ClientParticleRegistrar() {
            @Override
            public <T extends ParticleOptions> void register(ParticleType<T> type, HyphaPiraceaParticles.ParticleRegistration<T> registration) {
                ParticleFactoryRegistry.getInstance().register(type, registration::create);
            }
        });

        // start client tick
        ClientTickEvents.START_WORLD_TICK.register((minecraft) -> {
            HyphaPiraceaClient.startClientTick();
        });
    }

    public <E extends Entity> void registerEntityRenderer(EntityType<? extends E> entityType, EntityRendererProvider<E> entityRendererFactory) {
        EntityRendererRegistry.register(entityType, entityRendererFactory);
    }
}
