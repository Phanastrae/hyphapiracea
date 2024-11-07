package phanastrae.hyphapiracea.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.entity.status.HyphaPiraceaStatusEffects;
import phanastrae.hyphapiracea.item.HyphaPiraceaCreativeModeTabs;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static phanastrae.hyphapiracea.item.HyphaPiraceaCreativeModeTabs.OP_BLOCKS;

public class HyphaPiraceaFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		// mob effect registry
		HyphaPiraceaStatusEffects.init((name, mobEffect) -> Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, HyphaPiracea.id(name), mobEffect));

		// init registry entries
		HyphaPiracea.initRegistryEntries(new HyphaPiracea.RegistryListenerAdder() {
			@Override
			public <T> void addRegistryListener(Registry<T> registry, Consumer<BiConsumer<ResourceLocation, T>> source) {
				source.accept((rl, t) -> Registry.register(registry, rl, t));
			}
		});

		// common init
		HyphaPiracea.commonInit();

		// creative tabs
		setupCreativeTabs();

		// modify default components
		DefaultItemComponentEvents.MODIFY.register((context -> {
			HyphaPiracea.modifyDataComponents(new HyphaPiracea.ComponentModificationHelper() {
				@Override
				public <T> void modifyComponentsMap(Item item, DataComponentType<T> type, T component) {
					context.modify(item, builder -> builder.set(type, component));
				}
			});
		}));
	}

	public void setupCreativeTabs() {
		HyphaPiraceaCreativeModeTabs.setupEntries(new HyphaPiraceaCreativeModeTabs.Helper() {
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

			@Override
			public boolean operatorTabEnabled() {
				// fabric seems to hide the operator tab automatically, so we can just return true here
				return true;
			}
		});
	}
}