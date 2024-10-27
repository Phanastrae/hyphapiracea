package phanastrae.ywsanf;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import phanastrae.ywsanf.block.YWSaNFBlocks;
import phanastrae.ywsanf.block.entity.YWSaNFBlockEntityTypes;
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

        // blocks
        rla.addRegistryListener(BuiltInRegistries.BLOCK, YWSaNFBlocks::init);
        // items
        rla.addRegistryListener(BuiltInRegistries.ITEM, YWSaNFItems::init);

        // block entity types
        rla.addRegistryListener(BuiltInRegistries.BLOCK_ENTITY_TYPE, YWSaNFBlockEntityTypes::init);

        // particle types
        rla.addRegistryListener(BuiltInRegistries.PARTICLE_TYPE, YWSaNFParticleTypes::init);
    }

    @FunctionalInterface
    public interface RegistryListenerAdder {
        <T> void addRegistryListener(Registry<T> registry, Consumer<BiConsumer<ResourceLocation, T>> source);
    }
}
