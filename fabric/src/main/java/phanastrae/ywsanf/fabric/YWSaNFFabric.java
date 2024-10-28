package phanastrae.ywsanf.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.item.YWSaNFCreativeModeTabs;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class YWSaNFFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		// init registry entries
		YWSaNF.initRegistryEntries(new YWSaNF.RegistryListenerAdder() {
			@Override
			public <T> void addRegistryListener(Registry<T> registry, Consumer<BiConsumer<ResourceLocation, T>> source) {
				source.accept((rl, t) -> Registry.register(registry, rl, t));
			}
		});

		// creative tabs
		setupCreativeTabs();

		// modify default components
		DefaultItemComponentEvents.MODIFY.register((context -> {
			YWSaNF.modifyDataComponents(new YWSaNF.ComponentModificationHelper() {
				@Override
				public <T> void modifyComponentsMap(Item item, DataComponentType<T> type, T component) {
					context.modify(item, builder -> builder.set(type, component));
				}
			});
		}));
	}

	public void setupCreativeTabs() {
		YWSaNFCreativeModeTabs.setupEntries(new YWSaNFCreativeModeTabs.Helper() {
			@Override
			public void add(ResourceKey<CreativeModeTab> groupKey, ItemLike item) {
				ItemGroupEvents.modifyEntriesEvent(groupKey).register(entries -> entries.accept(item));
			}

			@Override
			public void add(ResourceKey<CreativeModeTab> groupKey, ItemLike... items) {
				ItemGroupEvents.modifyEntriesEvent(groupKey).register(entries -> {
					for(ItemLike item : items) {
						entries.accept(item);
					}
				});
			}

			@Override
			public void add(ResourceKey<CreativeModeTab> groupKey, ItemStack item) {
				ItemGroupEvents.modifyEntriesEvent(groupKey).register(entries -> entries.accept(item));
			}

			@Override
			public void add(ResourceKey<CreativeModeTab> groupKey, Collection<ItemStack> items) {
				ItemGroupEvents.modifyEntriesEvent(groupKey).register(entries -> {
					for(ItemStack item : items) {
						entries.accept(item);
					}
				});
			}

			@Override
			public void addAfter(ItemLike after, ResourceKey<CreativeModeTab> groupKey, ItemLike item) {
				ItemGroupEvents.modifyEntriesEvent(groupKey).register(entries -> entries.addAfter(after, item));
			}

			@Override
			public void addAfter(ItemStack after, ResourceKey<CreativeModeTab> groupKey, ItemStack item) {
				ItemGroupEvents.modifyEntriesEvent(groupKey).register(entries -> entries.addAfter(after, item));
			}

			@Override
			public void addAfter(ItemLike after, ResourceKey<CreativeModeTab> groupKey, ItemLike... items) {
				ItemGroupEvents.modifyEntriesEvent(groupKey).register(entries -> entries.addAfter(after, items));
			}

			@Override
			public void forTabRun(ResourceKey<CreativeModeTab> groupKey, BiConsumer<CreativeModeTab.ItemDisplayParameters, CreativeModeTab.Output> biConsumer) {
				ItemGroupEvents.modifyEntriesEvent(groupKey).register(entries -> {
					CreativeModeTab.ItemDisplayParameters displayContext = entries.getContext();
					biConsumer.accept(displayContext, entries);
				});
			}
		});
	}
}