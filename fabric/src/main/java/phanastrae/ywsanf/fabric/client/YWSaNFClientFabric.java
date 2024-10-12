package phanastrae.ywsanf.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import phanastrae.ywsanf.client.YWSaNFClient;

public class YWSaNFClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        YWSaNFClient.init();
    }
}
