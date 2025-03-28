package phanastrae.hyphapiracea.neoforge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GameShuttingDownEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.client.HyphaPiraceaClient;
import phanastrae.hyphapiracea.client.particle.HyphaPiraceaParticles;
import phanastrae.hyphapiracea.client.renderer.LeyfieldEnvironmentEffects;
import phanastrae.hyphapiracea.client.renderer.block.entity.HyphalConductorBlockEntityRenderer;
import phanastrae.hyphapiracea.client.renderer.entity.HyphaPiraceaEntityRenderers;
import phanastrae.hyphapiracea.client.renderer.entity.model.HyphaPiraceaEntityModelLayers;

import static net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_ENTITIES;
import static net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage.AFTER_SKY;

@Mod(value = HyphaPiracea.MOD_ID, dist = Dist.CLIENT)
public class HyphaPiraceaClientNeoForge {

    public HyphaPiraceaClientNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::onClientInit);

        // block color handlers
        modEventBus.addListener(this::registerBlockColorHandlers);

        // entity renderers
        modEventBus.addListener(this::registerEntityRenderers);

        // entity model layers
        modEventBus.addListener(this::registerEntityModelLayers);

        // particles
        modEventBus.addListener(this::registerParticleProviders);


        // client shutdown
        NeoForge.EVENT_BUS.addListener(this::onGameShutdown);

        // render leyfield effects
        NeoForge.EVENT_BUS.addListener(this::renderLevel);

        // render gui layers
        NeoForge.EVENT_BUS.addListener(this::renderGuiLayers);

        // start client tick
        NeoForge.EVENT_BUS.addListener(this::startClientTick);
    }

    public void onClientInit(FMLClientSetupEvent event) {
        // everything here needs to be multithread safe
        event.enqueueWork(HyphaPiraceaClient::init);
    }

    public void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        HyphaPiraceaEntityRenderers.init(event::registerEntityRenderer);
    }

    public void registerEntityModelLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        HyphaPiraceaEntityModelLayers.init(event::registerLayerDefinition);
    }

    public void registerParticleProviders(RegisterParticleProvidersEvent event) {
        HyphaPiraceaParticles.init(new HyphaPiraceaParticles.ClientParticleRegistrar() {
            @Override
            public <T extends ParticleOptions> void register(ParticleType<T> type, HyphaPiraceaParticles.ParticleRegistration<T> registration) {
                event.registerSpriteSet(type, registration::create);
            }
        });
    }

    public void registerBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        HyphaPiraceaClient.registerBlockColorHandlers(event::register);
    }

    public void renderGuiLayers(RenderGuiLayerEvent.Post event) {
        boolean hideGui = Minecraft.getInstance().options.hideGui;
        if(!hideGui) {
            ResourceLocation name = event.getName();
            if(name.equals(VanillaGuiLayers.SELECTED_ITEM_NAME)) {
                HyphaPiraceaClient.renderGuiOverlayItemName(event.getGuiGraphics());
            }
        }
    }

    public void startClientTick(LevelTickEvent.Pre event) {
        if(event.getLevel().isClientSide) {
            HyphaPiraceaClient.startClientTick();
        }
    }

    public void onGameShutdown(GameShuttingDownEvent event) {
        HyphaPiraceaClient.onClientStop(Minecraft.getInstance());
    }

    public void renderLevel(RenderLevelStageEvent event) {
        ClientLevel level = Minecraft.getInstance().level;
        if(level == null) return;

        RenderLevelStageEvent.Stage stage = event.getStage();
        if(stage.equals(AFTER_SKY)) {
            // render leyfield effects
            LeyfieldEnvironmentEffects.renderSky(event.getModelViewMatrix(), event.getPartialTick(), level, event.getProjectionMatrix());
        } else if(stage.equals(AFTER_ENTITIES)) {
            // render hyphalines
            HyphalConductorBlockEntityRenderer.renderHyphalines(event.getPoseStack(), event.getPartialTick(), event.getLevelRenderer(), event.getCamera(), level);
        }
    }
}
