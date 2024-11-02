package phanastrae.ywsanf.neoforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.client.YWSaNFClient;
import phanastrae.ywsanf.client.particle.YWSaNFParticles;
import phanastrae.ywsanf.client.renderer.entity.model.YWSaNFEntityModelLayers;

@Mod(value = YWSaNF.MOD_ID, dist = Dist.CLIENT)
public class YWSaNFClientNeoForge {

    public YWSaNFClientNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::onClientInit);

        // entity model layers
        modEventBus.addListener(this::registerEntityModelLayers);

        // particles
        modEventBus.addListener(this::registerParticleProviders);

        // render gui layers
        NeoForge.EVENT_BUS.addListener(this::renderGuiLayers);

        // start client tick
        NeoForge.EVENT_BUS.addListener(this::startClientTick);
    }

    public void onClientInit(FMLClientSetupEvent event) {
        // everything here needs to be multithread safe
        event.enqueueWork(YWSaNFClient::init);
    }

    public void registerEntityModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        YWSaNFEntityModelLayers.init(event::registerLayerDefinition);
    }

    public void registerParticleProviders(RegisterParticleProvidersEvent event) {
        YWSaNFParticles.init(new YWSaNFParticles.ClientParticleRegistrar() {
            @Override
            public <T extends ParticleOptions> void register(ParticleType<T> type, YWSaNFParticles.ParticleRegistration<T> registration) {
                event.registerSpriteSet(type, registration::create);
            }
        });
    }

    public void renderGuiLayers(RenderGuiLayerEvent.Post event) {
        boolean hideGui = Minecraft.getInstance().options.hideGui;
        if(!hideGui) {
            ResourceLocation name = event.getName();
            if(name.equals(VanillaGuiLayers.SELECTED_ITEM_NAME)) {
                YWSaNFClient.renderGuiOverlayItemName(event.getGuiGraphics());
            }
        }
    }

    public void startClientTick(ClientTickEvent event) {
        YWSaNFClient.startClientTick();
    }
}
