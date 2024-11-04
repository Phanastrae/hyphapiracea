package phanastrae.hyphapiracea.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import org.joml.Vector3f;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.block.HyphaPiraceaBlocks;
import phanastrae.hyphapiracea.component.HyphaPiraceaComponentTypes;
import phanastrae.hyphapiracea.component.type.KeyedDiscComponent;
import phanastrae.hyphapiracea.component.type.WireLineComponent;

import java.util.function.BiConsumer;

import static phanastrae.hyphapiracea.component.type.WireLineComponent.textureOf;

public class HyphaPiraceaItems {

    public static final BlockItem AZIMULDEY_MASS = ofBlock(HyphaPiraceaBlocks.AZIMULDEY_MASS);
    public static final BlockItem AZIMULIC_STEM = ofBlock(HyphaPiraceaBlocks.AZIMULIC_STEM);
    public static final BlockItem HYPHAL_NODE = ofBlock(HyphaPiraceaBlocks.HYPHAL_NODE);
    public static final BlockItem HYPHAL_STEM = ofBlock(HyphaPiraceaBlocks.HYPHAL_STEM);
    public static final BlockItem HYPHAL_CONDUCTOR = ofBlock(HyphaPiraceaBlocks.HYPHAL_CONDUCTOR);
    public static final BlockItem STORMSAP_CELL = ofBlock(HyphaPiraceaBlocks.STORMSAP_CELL);
    public static final BlockItem CREATIVE_CELL = ofBlock(HyphaPiraceaBlocks.CREATIVE_CELL, properties().rarity(Rarity.EPIC));
    public static final BlockItem HYPHAL_AMMETER = ofBlock(HyphaPiraceaBlocks.HYPHAL_AMMETER);
    public static final BlockItem HYPHAL_VOLTMETER = ofBlock(HyphaPiraceaBlocks.HYPHAL_VOLTMETER);
    public static final BlockItem CIRCUIT_SWITCH = ofBlock(HyphaPiraceaBlocks.CIRCUIT_SWITCH);
    public static final BlockItem LEYFIELD_MAGNETOMETER_BLOCK = ofBlock(HyphaPiraceaBlocks.LEYFIELD_MAGNETOMETER_BLOCK);
    public static final BlockItem ELECTROMAGNETIC_DUST_BOX = ofBlock(HyphaPiraceaBlocks.ELECTROMAGNETIC_DUST_BOX);
    public static final BlockItem PIRACEATIC_LEUKBOX = ofBlock(HyphaPiraceaBlocks.PIRACEATIC_LEUKBOX);
    public static final BlockItem PIRACEATIC_GLOBGLASS = ofBlock(HyphaPiraceaBlocks.PIRACEATIC_GLOBGLASS);

    public static final Item HYPHALINE = new Item(properties().component(HyphaPiraceaComponentTypes.WIRE_LINE_COMPONENT,
            new WireLineComponent(24, 40, 0.004F, 3)
    ));
    public static final Item OGRAL_HYPHALINE = new Item(properties().component(HyphaPiraceaComponentTypes.WIRE_LINE_COMPONENT,
            new WireLineComponent(48, 60, 0.002F, 5, textureOf("ogral_hyphaline"), new Vector3f(0.8F, 0.9F, 0.5F), new Vector3f(0.4F, 0.6F, 0.3F))
    ));
    public static final Item FERRIC_WARDLINE = new Item(properties().component(HyphaPiraceaComponentTypes.WIRE_LINE_COMPONENT,
            new WireLineComponent(13, 0, 0.026F, 13, textureOf("ferric_wardline"), new Vector3f(0.6F, 0.6F, 0.6F), new Vector3f(0.4F, 0.4F, 0.4F))
    ));
    public static final Item LEYFIELD_MAGNETOMETER = new MagnetometerItem(properties().stacksTo(1));
    public static final Item ELECTROMAGNETIC_DUST = new ElectromagneticDustItem(properties());
    public static final Item KEYED_DISC = new KeyedDiscItem(properties().stacksTo(1).rarity(Rarity.UNCOMMON).component(HyphaPiraceaComponentTypes.KEYED_DISC_COMPONENT, new KeyedDiscComponent(ResourceLocation.fromNamespaceAndPath("minecraft", "fortress"), 48.0F, 0.000001F, 8)));
    public static final Item POSITIVE_CHARGEBALL = new ChargeballItem(properties(),1, 0);
    public static final Item NEGATIVE_CHARGEBALL = new ChargeballItem(properties(),-1, 0);
    public static final Item NORTHERN_CHARGEBALL = new ChargeballItem(properties(),0, 1);
    public static final Item SOUTHERN_CHARGEBALL = new ChargeballItem(properties(),0, -1);
    public static final Item POSITIVE_SPOREBERRY = new Item(properties().food(HyphaPiraceaFoodProperties.POSITIVE_SPOREBERRY));
    public static final Item NEGATIVE_SPOREBERRY = new Item(properties().food(HyphaPiraceaFoodProperties.NEGATIVE_SPOREBERRY));
    public static final Item NORTHERN_SPOREBERRY = new Item(properties().food(HyphaPiraceaFoodProperties.NORTHERN_SPOREBERRY));
    public static final Item SOUTHERN_SPOREBERRY = new Item(properties().food(HyphaPiraceaFoodProperties.SOUTHERN_SPOREBERRY));
    public static final Item PIRACEATIC_GLOB = new Item(properties());

    public static void init(BiConsumer<ResourceLocation, Item> r) {
        BiConsumer<String, Item> rwt = (s, i) -> { // register and add to creative mode tab
            r.accept(id(s), i);
            HyphaPiraceaCreativeModeTabs.addItemToHyphaPiraceaTab(i);
        };

        rwt.accept("azimuldey_mass", AZIMULDEY_MASS);
        rwt.accept("azimulic_stem", AZIMULIC_STEM);
        rwt.accept("hyphal_node", HYPHAL_NODE);
        rwt.accept("hyphal_stem", HYPHAL_STEM);
        rwt.accept("hyphaline", HYPHALINE);
        rwt.accept("ogral_hyphaline", OGRAL_HYPHALINE);
        rwt.accept("ferric_wardline", FERRIC_WARDLINE);
        rwt.accept("hyphal_conductor", HYPHAL_CONDUCTOR);
        rwt.accept("stormsap_cell", STORMSAP_CELL);
        rwt.accept("creative_cell", CREATIVE_CELL);
        rwt.accept("ammeter_block", HYPHAL_AMMETER);
        rwt.accept("voltmeter_block", HYPHAL_VOLTMETER);
        rwt.accept("circuit_switch", CIRCUIT_SWITCH);
        rwt.accept("magnetometer", LEYFIELD_MAGNETOMETER);
        rwt.accept("magnetometer_block", LEYFIELD_MAGNETOMETER_BLOCK);
        rwt.accept("electromagnetic_dust", ELECTROMAGNETIC_DUST);
        rwt.accept("electromagnetic_dust_box", ELECTROMAGNETIC_DUST_BOX);
        rwt.accept("piraceatic_leukbox", PIRACEATIC_LEUKBOX);
        rwt.accept("keyed_disc", KEYED_DISC);
        rwt.accept("positive_chargeball", POSITIVE_CHARGEBALL);
        rwt.accept("negative_chargeball", NEGATIVE_CHARGEBALL);
        rwt.accept("northern_chargeball", NORTHERN_CHARGEBALL);
        rwt.accept("southern_chargeball", SOUTHERN_CHARGEBALL);
        rwt.accept("positive_sporeberry", POSITIVE_SPOREBERRY);
        rwt.accept("negative_sporeberry", NEGATIVE_SPOREBERRY);
        rwt.accept("northern_sporeberry", NORTHERN_SPOREBERRY);
        rwt.accept("southern_sporeberry", SOUTHERN_SPOREBERRY);
        rwt.accept("piraceatic_glob", PIRACEATIC_GLOB);
        rwt.accept("piraceatic_globglass", PIRACEATIC_GLOBGLASS);
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
