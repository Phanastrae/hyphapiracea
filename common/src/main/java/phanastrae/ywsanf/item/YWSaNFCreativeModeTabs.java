package phanastrae.ywsanf.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.services.XPlatInterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

public class YWSaNFCreativeModeTabs {

    public static final CreativeModeTab YWSANF_TAB = XPlatInterface.INSTANCE.createCreativeModeTabBuilder()
            .icon(YWSaNFItems.KEYED_DISC::getDefaultInstance)
            .title(Component.translatable("itemGroup.ywsanf"))
            .build();
    public static final ResourceKey<CreativeModeTab> YWSANF_RESOURCE_KEY = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), YWSaNF.id("ywsanf"));

    private static final List<ItemStack> QUEUED_TAB_ITEMS = new ArrayList<>();

    public static void init(BiConsumer<ResourceLocation, CreativeModeTab> r) {
        r.accept(id("ywsanf"), YWSANF_TAB);
    }

    public static void addItemToYWSaNFTab(ItemLike item) {
        addItemToYWSaNFTab(new ItemStack(item));
    }

    public static void addItemToYWSaNFTab(ItemStack itemStack) {
        QUEUED_TAB_ITEMS.add(itemStack);
    }

    public static void setupEntries(Helper helper) {
        addQueuedItems(helper);
    }

    private static void addQueuedItems(Helper helper) {
        helper.add(YWSANF_RESOURCE_KEY, QUEUED_TAB_ITEMS);
    }

    private static ResourceLocation id(String path) {
        return YWSaNF.id(path);
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
    }
}