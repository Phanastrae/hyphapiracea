package phanastrae.hyphapiracea.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.block.HyphaPiraceaBlocks;
import phanastrae.hyphapiracea.component.HyphaPiraceaComponentTypes;
import phanastrae.hyphapiracea.component.type.KeyedDiscComponent;
import phanastrae.hyphapiracea.component.type.WireLineComponent;

import java.util.function.BiConsumer;

public class HyphaPiraceaItems {

    public static final BlockItem PIRACEATIC_LEUKBOX = ofBlock(HyphaPiraceaBlocks.PIRACEATIC_LEUKBOX);
    public static final BlockItem HYPHAL_CONDUCTOR = ofBlock(HyphaPiraceaBlocks.HYPHAL_CONDUCTOR);
    public static final BlockItem LEYFIELD_MAGNETOMETER_BLOCK = ofBlock(HyphaPiraceaBlocks.LEYFIELD_MAGNETOMETER_BLOCK);
    public static final BlockItem HYPHAL_NODE = ofBlock(HyphaPiraceaBlocks.HYPHAL_NODE);
    public static final BlockItem HYPHAL_AMMETER = ofBlock(HyphaPiraceaBlocks.HYPHAL_AMMETER);
    public static final BlockItem HYPHAL_VOLTMETER = ofBlock(HyphaPiraceaBlocks.HYPHAL_VOLTMETER);
    public static final BlockItem STORMSAP_CELL = ofBlock(HyphaPiraceaBlocks.STORMSAP_CELL);
    public static final BlockItem AZIMULDEY_MASS = ofBlock(HyphaPiraceaBlocks.AZIMULDEY_MASS);
    public static final BlockItem CREATIVE_CELL = ofBlock(HyphaPiraceaBlocks.CREATIVE_CELL, properties().rarity(Rarity.EPIC));
    public static final BlockItem CIRCUIT_SWITCH = ofBlock(HyphaPiraceaBlocks.CIRCUIT_SWITCH);

    public static final Item KEYED_DISC = new KeyedDiscItem(properties().stacksTo(1).rarity(Rarity.UNCOMMON).component(HyphaPiraceaComponentTypes.KEYED_DISC_COMPONENT, new KeyedDiscComponent(ResourceLocation.fromNamespaceAndPath("minecraft", "fortress"), 48.0F, 0.000001F)));
    public static final Item HYPHALINE = new HyphalineItem(properties().component(HyphaPiraceaComponentTypes.WIRE_LINE_COMPONENT, new WireLineComponent(24, 40, 0.004F)));
    public static final Item LEYFIELD_MAGNETOMETER = new MagnetometerItem(properties().stacksTo(1));

    public static void init(BiConsumer<ResourceLocation, Item> r) {
        BiConsumer<String, Item> rwt = (s, i) -> { // register and add to creative mode tab
            r.accept(id(s), i);
            HyphaPiraceaCreativeModeTabs.addItemToHyphaPiraceaTab(i);
        };

        rwt.accept("keyed_disc", KEYED_DISC);
        rwt.accept("piraceatic_leukbox", PIRACEATIC_LEUKBOX);
        rwt.accept("hyphal_conductor", HYPHAL_CONDUCTOR);
        rwt.accept("hyphaline", HYPHALINE);
        rwt.accept("magnetometer", LEYFIELD_MAGNETOMETER);
        rwt.accept("magnetometer_block", LEYFIELD_MAGNETOMETER_BLOCK);
        rwt.accept("hyphal_node", HYPHAL_NODE);
        rwt.accept("ammeter_block", HYPHAL_AMMETER);
        rwt.accept("voltmeter_block", HYPHAL_VOLTMETER);
        rwt.accept("stormsap_cell", STORMSAP_CELL);
        rwt.accept("azimuldey_mass", AZIMULDEY_MASS);
        rwt.accept("creative_cell", CREATIVE_CELL);
        rwt.accept("circuit_switch", CIRCUIT_SWITCH);
    }

    private static ResourceLocation id(String path) {
        return HyphaPiracea.id(path);
    }

    private static Item.Properties properties() {
        return new Item.Properties();
    }

    private static BlockItem ofBlock(Block block) {
        return ofBlock(block, properties());
    }

    private static BlockItem ofBlock(Block block, Item.Properties properties) {
        return new BlockItem(block, properties);
    }
}
