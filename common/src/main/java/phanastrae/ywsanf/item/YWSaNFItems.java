package phanastrae.ywsanf.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import phanastrae.ywsanf.YWSaNF;

import java.util.function.BiConsumer;

public class YWSaNFItems {

    public static final Item KEYED_DISC = new Item(settings());

    public static void init(BiConsumer<ResourceLocation, Item> r) {
        BiConsumer<ResourceLocation, Item> rwt = (rl, i) -> { // register and add to creative mode tab
            r.accept(rl, i);
            YWSaNFCreativeModeTabs.addItemToYWSaNFTab(i);
        };

        rwt.accept(id("keyed_disc"), KEYED_DISC);
    }

    private static ResourceLocation id(String path) {
        return YWSaNF.id(path);
    }

    public static Item.Properties settings() {
        return new Item.Properties();
    }
}
