package phanastrae.hyphapiracea.neoforge;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.entity.status.HyphaPiraceaStatusEffects;
import phanastrae.hyphapiracea.item.HyphaPiraceaCreativeModeTabs;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod(HyphaPiracea.MOD_ID)
public class HyphaPiraceaNeoForge {

    public HyphaPiraceaNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        setupModBusEvents(modEventBus);
        setupGameBusEvents(NeoForge.EVENT_BUS);
    }

    public void setupModBusEvents(IEventBus modEventBus) {
        // mob effect registry
        DeferredRegister<MobEffect> mobEffectDeferredRegister = DeferredRegister.create(Registries.MOB_EFFECT, HyphaPiracea.MOD_ID);
        mobEffectDeferredRegister.register(modEventBus);
        HyphaPiraceaStatusEffects.init((name, mobEffect) -> mobEffectDeferredRegister.register(name, () -> mobEffect));

        // init registry entries
        HyphaPiracea.initRegistryEntries(new HyphaPiracea.RegistryListenerAdder() {
            @Override
            public <T> void addRegistryListener(Registry<T> registry, Consumer<BiConsumer<ResourceLocation, T>> source) {
                modEventBus.addListener((RegisterEvent event) -> {
                    ResourceKey<? extends Registry<T>> registryKey = registry.key();
                    if(registryKey.equals(event.getRegistryKey())) {
                        source.accept((resourceLocation, t) -> event.register(registryKey, resourceLocation, () -> t));
                    }
                });
            }
        });

        // common init
        modEventBus.addListener(this::commonInit);

        // creative tabs
        modEventBus.addListener(this::buildCreativeModeTabContents);

        // modify default components
        modEventBus.addListener(this::modifyDefaultComponents);
    }

    public void setupGameBusEvents(IEventBus gameEventBus) {
        gameEventBus.addListener(this::addTooltips);
    }

    public void commonInit(FMLCommonSetupEvent event) {
        // everything here needs to be multithread safe
        event.enqueueWork(HyphaPiracea::commonInit);
    }

    public void buildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        ResourceKey<CreativeModeTab> eventKey = event.getTabKey();
        HyphaPiraceaCreativeModeTabs.setupEntries(new HyphaPiraceaCreativeModeTabs.Helper() {
            @Override
            public void add(ResourceKey<CreativeModeTab> groupKey, ItemLike item) {
                if(eventKey.equals(groupKey)) {
                    event.accept(item);
                }
            }

            @Override
            public void add(ResourceKey<CreativeModeTab> groupKey, ItemLike... items) {
                if(eventKey.equals(groupKey)) {
                    for(ItemLike item : items) {
                        event.accept(item);
                    }
                }
            }
            @Override
            public void add(ResourceKey<CreativeModeTab> groupKey, ItemStack item) {
                if(eventKey.equals(groupKey)) {
                    event.accept(item);
                }
            }

            @Override
            public void add(ResourceKey<CreativeModeTab> groupKey, Collection<ItemStack> items) {
                if(eventKey.equals(groupKey)) {
                    for(ItemStack item : items) {
                        event.accept(item);
                    }
                }
            }

            @Override
            public void addAfter(ItemLike after, ResourceKey<CreativeModeTab> groupKey, ItemLike item) {
                if(eventKey.equals(groupKey)) {
                    event.insertAfter(new ItemStack(after), new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                }
            }

            @Override
            public void addAfter(ItemStack after, ResourceKey<CreativeModeTab> groupKey, ItemStack item) {
                if(eventKey.equals(groupKey)) {
                    event.insertAfter(after, item, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                }
            }

            @Override
            public void addAfter(ItemLike after, ResourceKey<CreativeModeTab> groupKey, ItemLike... items) {
                if(eventKey.equals(groupKey)) {
                    for(ItemLike item : items) {
                        event.insertAfter(new ItemStack(after), new ItemStack(item), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                    }
                }
            }

            @Override
            public void forTabRun(ResourceKey<CreativeModeTab> groupKey, BiConsumer<CreativeModeTab.ItemDisplayParameters, CreativeModeTab.Output> biConsumer) {
                if(eventKey.equals(groupKey)) {
                    biConsumer.accept(event.getParameters(), event);
                }
            }

            @Override
            public boolean operatorTabEnabled() {
                return event.getParameters().hasPermissions();
            }
        });
    }

    public void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        HyphaPiracea.modifyDataComponents(new HyphaPiracea.ComponentModificationHelper() {
            @Override
            public <T> void modifyComponentsMap(Item item, DataComponentType<T> type, T component) {
                event.modify(item, builder -> builder.set(type, component));
            }
        });
    }

    public void addTooltips(ItemTooltipEvent event) {
        HyphaPiracea.addTooltips(event.getItemStack(), event.getContext(), event.getToolTip()::add, event.getFlags());
    }
}
