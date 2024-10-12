package phanastrae.ywsanf.neoforge.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.client.YWSaNFClient;

@Mod(value = YWSaNF.MOD_ID, dist = Dist.CLIENT)
public class YWSaNFClientNeoForge {

    public YWSaNFClientNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::onClientInit);
    }

    public void onClientInit(FMLClientSetupEvent event) {
        // everything here needs to be multithread safe
        event.enqueueWork(YWSaNFClient::init);
    }
}
