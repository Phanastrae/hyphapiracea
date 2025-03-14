package phanastrae.hyphapiracea.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.services.XPlatInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

public class HyphaPiraceaCreativeModeTabs {
    public static final ResourceKey<CreativeModeTab> BUILDING_BLOCKS = createKey("building_blocks");
    public static final ResourceKey<CreativeModeTab> COLORED_BLOCKS = createKey("colored_blocks");
    public static final ResourceKey<CreativeModeTab> NATURAL_BLOCKS = createKey("natural_blocks");
    public static final ResourceKey<CreativeModeTab> FUNCTIONAL_BLOCKS = createKey("functional_blocks");
    public static final ResourceKey<CreativeModeTab> REDSTONE_BLOCKS = createKey("redstone_blocks");
    public static final ResourceKey<CreativeModeTab> TOOLS_AND_UTILITIES = createKey("tools_and_utilities");
    public static final ResourceKey<CreativeModeTab> COMBAT = createKey("combat");
    public static final ResourceKey<CreativeModeTab> FOOD_AND_DRINKS = createKey("food_and_drinks");
    public static final ResourceKey<CreativeModeTab> INGREDIENTS = createKey("ingredients");
    public static final ResourceKey<CreativeModeTab> SPAWN_EGGS = createKey("spawn_eggs");
    public static final ResourceKey<CreativeModeTab> OP_BLOCKS = createKey("op_blocks");

    public static final CreativeModeTab HYPHAPIRACEA_TAB = XPlatInterface.INSTANCE.createCreativeModeTabBuilder()
            .icon(HyphaPiraceaItems.KEYED_DISC::getDefaultInstance)
            .title(Component.translatable("itemGroup.hyphapiracea.group"))
            .build();
    public static final ResourceKey<CreativeModeTab> HYPHAPIRACEA_RESOURCE_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), HyphaPiracea.id("hyphapiracea"));

    private static final List<ItemStack> QUEUED_TAB_ITEMS = new ArrayList<>();

    public static void init(BiConsumer<ResourceLocation, CreativeModeTab> r) {
        r.accept(id("hyphapiracea"), HYPHAPIRACEA_TAB);
    }

    public static void addItemToHyphaPiraceaTab(ItemLike item) {
        addItemToHyphaPiraceaTab(new ItemStack(item));
    }

    public static void addItemToHyphaPiraceaTab(ItemStack itemStack) {
        QUEUED_TAB_ITEMS.add(itemStack);
    }

    public static void setupEntries(Helper helper) {
        addQueuedItems(helper);

        helper.add(BUILDING_BLOCKS,
                HyphaPiraceaItems.PIRACEATIC_GLOBGLASS
        );

        helper.add(NATURAL_BLOCKS,
                HyphaPiraceaItems.AZIMULDEY_MASS,
                HyphaPiraceaItems.AZIMULIC_STEM
        );

        helper.add(FUNCTIONAL_BLOCKS,
                HyphaPiraceaItems.PIRACEATIC_LEUKBOX
        );

        helper.add(REDSTONE_BLOCKS,
                HyphaPiraceaItems.HYPHALINE,
                HyphaPiraceaItems.OGRAL_HYPHALINE,
                HyphaPiraceaItems.FERRIC_WARDLINE,
                HyphaPiraceaItems.HYPHAL_NODE,
                HyphaPiraceaItems.HYPHAL_STEM,
                HyphaPiraceaItems.HYPHAL_CONDUCTOR,
                HyphaPiraceaItems.STORMSAP_CELL,
                HyphaPiraceaItems.HYPHAL_AMMETER,
                HyphaPiraceaItems.HYPHAL_VOLTMETER,
                HyphaPiraceaItems.CIRCUIT_SWITCH,
                HyphaPiraceaItems.LEYFIELD_MAGNETOMETER_BLOCK,
                HyphaPiraceaItems.ELECTROMAGNETIC_DUST_BOX,
                HyphaPiraceaItems.PIRACEATIC_LEUKBOX
        );

        helper.add(TOOLS_AND_UTILITIES,
                HyphaPiraceaItems.LEYFIELD_MAGNETOMETER,
                HyphaPiraceaItems.ELECTROMAGNETIC_DUST,
                HyphaPiraceaItems.KEYED_DISC
        );

        helper.add(COMBAT,
                HyphaPiraceaItems.POSITIVE_CHARGEBALL,
                HyphaPiraceaItems.NEGATIVE_CHARGEBALL,
                HyphaPiraceaItems.NORTHERN_CHARGEBALL,
                HyphaPiraceaItems.SOUTHERN_CHARGEBALL
        );

        helper.add(FOOD_AND_DRINKS,
                HyphaPiraceaItems.POSITIVE_SPOREBERRY,
                HyphaPiraceaItems.NEGATIVE_SPOREBERRY,
                HyphaPiraceaItems.NORTHERN_SPOREBERRY,
                HyphaPiraceaItems.SOUTHERN_SPOREBERRY
        );

        helper.add(INGREDIENTS,
                HyphaPiraceaItems.PIRACEATIC_GLOB
        );

        if(helper.operatorTabEnabled()) {
            helper.add(OP_BLOCKS,
                    HyphaPiraceaItems.CREATIVE_CELL,
                    HyphaPiraceaItems.LEUKBOX_LOCK
            );
        }
    }

    private static void addQueuedItems(Helper helper) {
        helper.add(HYPHAPIRACEA_RESOURCE_KEY, QUEUED_TAB_ITEMS);
    }

    private static ResourceLocation id(String path) {
        return HyphaPiracea.id(path);
    }

    private static ResourceKey<CreativeModeTab> createKey(String name) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.withDefaultNamespace(name));
    }

    public static abstract class Helper {
        public abstract void add(ResourceKey<CreativeModeTab> groupKey, ItemLike item);

        public abstract void add(ResourceKey<CreativeModeTab> groupKey, ItemLike... items);

        public abstract void add(ResourceKey<CreativeModeTab> groupKey, ItemStack item);

        public abstract void add(ResourceKey<CreativeModeTab> groupKey, Collection<ItemStack> items);

        public abstract void addAfter(ItemLike after, ResourceKey<CreativeModeTab> groupKey, ItemLike item);

        public abstract void addAfter(ItemStack after, ResourceKey<CreativeModeTab> groupKey, ItemStack item);

        public abstract void addAfter(ItemLike after, ResourceKey<CreativeModeTab> groupKey, ItemLike... items);

        public abstract void forTabRun(ResourceKey<CreativeModeTab> groupKey, BiConsumer<CreativeModeTab.ItemDisplayParameters, CreativeModeTab.Output> biConsumer);

        public abstract boolean operatorTabEnabled();
    }
}
