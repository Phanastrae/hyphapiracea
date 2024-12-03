package phanastrae.hyphapiracea;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.block.DispenserBlock;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phanastrae.hyphapiracea.block.HyphaPiraceaBlocks;
import phanastrae.hyphapiracea.block.HyphaPiraceaDispenserBehavior;
import phanastrae.hyphapiracea.block.entity.HyphaPiraceaBlockEntityTypes;
import phanastrae.hyphapiracea.component.HyphaPiraceaComponentTypes;
import phanastrae.hyphapiracea.component.type.WireLineComponent;
import phanastrae.hyphapiracea.entity.HyphaPiraceaEntityTypes;
import phanastrae.hyphapiracea.item.HyphaPiraceaCreativeModeTabs;
import phanastrae.hyphapiracea.item.HyphaPiraceaItems;
import phanastrae.hyphapiracea.particle.HyphaPiraceaParticleTypes;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class HyphaPiracea {
    public static final String MOD_ID = "hyphapiracea";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void initRegistryEntries(RegistryListenerAdder rla) {
        // creative mode tabs
        rla.addRegistryListener(BuiltInRegistries.CREATIVE_MODE_TAB, HyphaPiraceaCreativeModeTabs::init);

        // data components
        rla.addRegistryListener(BuiltInRegistries.DATA_COMPONENT_TYPE, HyphaPiraceaComponentTypes::init);

        // blocks
        rla.addRegistryListener(BuiltInRegistries.BLOCK, HyphaPiraceaBlocks::init);
        // items
        rla.addRegistryListener(BuiltInRegistries.ITEM, HyphaPiraceaItems::init);

        // block entity types
        rla.addRegistryListener(BuiltInRegistries.BLOCK_ENTITY_TYPE, HyphaPiraceaBlockEntityTypes::init);

        // entity types
        rla.addRegistryListener(BuiltInRegistries.ENTITY_TYPE, HyphaPiraceaEntityTypes::init);

        // particle types
        rla.addRegistryListener(BuiltInRegistries.PARTICLE_TYPE, HyphaPiraceaParticleTypes::init);
    }

    public static void commonInit() {
        // dispenser behaviors
        HyphaPiraceaDispenserBehavior.init();
    }

    public static void modifyDataComponents(ComponentModificationHelper helper) {
        helper.modifyComponentsMap(Items.STRING, HyphaPiraceaComponentTypes.WIRE_LINE_COMPONENT,
                new WireLineComponent(6, 8, 0.1F, 5, WireLineComponent.textureOf("string"), new Vector3f(0.9F, 0.9F, 0.9F), new Vector3f(0.7F, 0.7F, 0.7F)));
    }

    public static void addTooltips(ItemStack stack, Item.TooltipContext tooltipContext, Consumer<Component> componentConsumer, TooltipFlag tooltipFlag) {
        addToTooltip(stack, HyphaPiraceaComponentTypes.KEYED_DISC_COMPONENT, tooltipContext, componentConsumer, tooltipFlag);
        addToTooltip(stack, HyphaPiraceaComponentTypes.WIRE_LINE_COMPONENT, tooltipContext, componentConsumer, tooltipFlag);
    }

    private static <T extends TooltipProvider> void addToTooltip(
            ItemStack stack, DataComponentType<T> component, Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag
    ) {
        T tooltipProvider = stack.get(component);
        if (tooltipProvider != null) {
            tooltipProvider.addToTooltip(context, tooltipAdder, tooltipFlag);
        }
    }

    @FunctionalInterface
    public interface RegistryListenerAdder {
        <T> void addRegistryListener(Registry<T> registry, Consumer<BiConsumer<ResourceLocation, T>> source);
    }

    @FunctionalInterface
    public interface ComponentModificationHelper {
        <T> void modifyComponentsMap(Item item, DataComponentType<T> type, T component);
    }
}
