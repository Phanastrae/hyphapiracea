package phanastrae.hyphapiracea.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.block.HyphaPiraceaBlocks;
import phanastrae.hyphapiracea.component.HyphaPiraceaComponentTypes;
import phanastrae.hyphapiracea.component.type.WireLineComponent;

import java.util.function.BiConsumer;

public class HyphaPiraceaItems {

    public static final BlockItem LEUKBOX = ofBlock(HyphaPiraceaBlocks.LEUKBOX);
    public static final BlockItem HYPHAL_CONDUCTOR = ofBlock(HyphaPiraceaBlocks.HYPHAL_CONDUCTOR);
    public static final BlockItem MAGNETOMETER_BLOCK = ofBlock(HyphaPiraceaBlocks.MAGNETOMETER_BLOCK);
    public static final BlockItem GALVANOCARPIC_BULB = ofBlock(HyphaPiraceaBlocks.GALVANOCARPIC_BULB);
    public static final BlockItem AMMETER_BLOCK = ofBlock(HyphaPiraceaBlocks.AMMETER_BLOCK);
    public static final BlockItem VOLTMETER_BLOCK = ofBlock(HyphaPiraceaBlocks.VOLTMETER_BLOCK);
    public static final BlockItem STORMSAP_CELL = ofBlock(HyphaPiraceaBlocks.STORMSAP_CELL);

    public static final Item KEYED_DISC = new KeyedDiscItem(properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    public static final Item HYPHALINE = new HyphalineItem(properties().component(HyphaPiraceaComponentTypes.WIRE_LINE_COMPONENT, new WireLineComponent(24, 40, 0.004F)));
    public static final Item MAGNETOMETER = new MagnetometerItem(properties().stacksTo(1));

    public static void init(BiConsumer<ResourceLocation, Item> r) {
        BiConsumer<String, Item> rwt = (s, i) -> { // register and add to creative mode tab
            r.accept(id(s), i);
            HyphaPiraceaCreativeModeTabs.addItemToHyphaPiraceaTab(i);
        };

        rwt.accept("keyed_disc", KEYED_DISC);
        rwt.accept("leukbox", LEUKBOX);
        rwt.accept("hyphal_conductor", HYPHAL_CONDUCTOR);
        rwt.accept("hyphaline", HYPHALINE);
        rwt.accept("magnetometer", MAGNETOMETER);
        rwt.accept("magnetometer_block", MAGNETOMETER_BLOCK);
        rwt.accept("galvanocarpic_bulb", GALVANOCARPIC_BULB);
        rwt.accept("ammeter_block", AMMETER_BLOCK);
        rwt.accept("voltmeter_block", VOLTMETER_BLOCK);
        rwt.accept("stormsap_cell", STORMSAP_CELL);
    }

    private static ResourceLocation id(String path) {
        return HyphaPiracea.id(path);
    }

    private static Item.Properties properties() {
        return new Item.Properties();
    }

    private static BlockItem ofBlock(Block block) {
        return new BlockItem(block, properties());
    }
}
