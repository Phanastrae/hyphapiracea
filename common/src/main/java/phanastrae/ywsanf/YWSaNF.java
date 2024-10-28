package phanastrae.ywsanf;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phanastrae.ywsanf.block.YWSaNFBlocks;
import phanastrae.ywsanf.block.entity.YWSaNFBlockEntityTypes;
import phanastrae.ywsanf.component.YWSaNFComponentTypes;
import phanastrae.ywsanf.component.type.WireLineComponent;
import phanastrae.ywsanf.item.YWSaNFCreativeModeTabs;
import phanastrae.ywsanf.item.YWSaNFItems;
import phanastrae.ywsanf.particle.YWSaNFParticleTypes;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class YWSaNF {
    public static final String MOD_ID = "ywsanf";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static void initRegistryEntries(RegistryListenerAdder rla) {
        // creative mode tabs
        rla.addRegistryListener(BuiltInRegistries.CREATIVE_MODE_TAB, YWSaNFCreativeModeTabs::init);

        // data components
        rla.addRegistryListener(BuiltInRegistries.DATA_COMPONENT_TYPE, YWSaNFComponentTypes::init);

        // blocks
        rla.addRegistryListener(BuiltInRegistries.BLOCK, YWSaNFBlocks::init);
        // items
        rla.addRegistryListener(BuiltInRegistries.ITEM, YWSaNFItems::init);

        // block entity types
        rla.addRegistryListener(BuiltInRegistries.BLOCK_ENTITY_TYPE, YWSaNFBlockEntityTypes::init);

        // particle types
        rla.addRegistryListener(BuiltInRegistries.PARTICLE_TYPE, YWSaNFParticleTypes::init);
    }

    public static void modifyDataComponents(ComponentModificationHelper helper) {
        helper.modifyComponentsMap(Items.STRING, YWSaNFComponentTypes.WIRE_LINE_COMPONENT,
                new WireLineComponent(6, 8, 0.1F, WireLineComponent.textureOf("string"), new Vector3f(0.9F, 0.9F, 0.9F), new Vector3f(0.7F, 0.7F, 0.7F)));
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
