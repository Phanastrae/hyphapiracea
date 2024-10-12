package phanastrae.ywsanf.client;

import phanastrae.ywsanf.client.renderer.block.entity.YWSaNFBlockEntityRenderers;

public class YWSaNFClient {

    public static void init() {
        // register block entity renderers
        YWSaNFBlockEntityRenderers.init();
    }
}
