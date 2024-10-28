package phanastrae.ywsanf.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.block.YWSaNFBlocks;
import phanastrae.ywsanf.component.YWSaNFComponentTypes;
import phanastrae.ywsanf.component.type.WireLineComponent;

import java.util.function.BiConsumer;

public class YWSaNFItems {

    public static final BlockItem LEUKBOX = ofBlock(YWSaNFBlocks.LEUKBOX);
    public static final BlockItem HYPHAL_CONDUCTOR = ofBlock(YWSaNFBlocks.HYPHAL_CONDUCTOR);
    public static final BlockItem MAGNETOMETER_BLOCK = ofBlock(YWSaNFBlocks.MAGNETOMETER_BLOCK);

    public static final Item KEYED_DISC = new KeyedDiscItem(properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final Item HYPHALINE = new HyphalineItem(properties().component(YWSaNFComponentTypes.WIRE_LINE_COMPONENT, new WireLineComponent(24, 40)));
    public static final Item MAGNETOMETER = new MagnetometerItem(properties().stacksTo(1));

    public static void init(BiConsumer<ResourceLocation, Item> r) {
        BiConsumer<String, Item> rwt = (s, i) -> { // register and add to creative mode tab
            r.accept(id(s), i);
            YWSaNFCreativeModeTabs.addItemToYWSaNFTab(i);
        };

        rwt.accept("keyed_disc", KEYED_DISC);
        rwt.accept("leukbox", LEUKBOX);
        rwt.accept("hyphal_conductor", HYPHAL_CONDUCTOR);
        rwt.accept("hyphaline", HYPHALINE);
        rwt.accept("magnetometer", MAGNETOMETER);
        rwt.accept("magnetometer_block", MAGNETOMETER_BLOCK);
    }

    private static ResourceLocation id(String path) {
        return YWSaNF.id(path);
    }

    private static Item.Properties properties() {
        return new Item.Properties();
    }

    private static BlockItem ofBlock(Block block) {
        return new BlockItem(block, properties());
    }
}
