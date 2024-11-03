package phanastrae.hyphapiracea.component;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.component.type.KeyedDiscComponent;
import phanastrae.hyphapiracea.component.type.WireLineComponent;

import java.util.function.BiConsumer;

public class HyphaPiraceaComponentTypes {

    public static final DataComponentType<WireLineComponent> WIRE_LINE_COMPONENT =
            DataComponentType.<WireLineComponent>builder().persistent(WireLineComponent.CODEC).networkSynchronized(WireLineComponent.PACKET_CODEC).cacheEncoding().build();
    public static final DataComponentType<KeyedDiscComponent> KEYED_DISC_COMPONENT =
            DataComponentType.<KeyedDiscComponent>builder().persistent(KeyedDiscComponent.CODEC).networkSynchronized(KeyedDiscComponent.PACKET_CODEC).cacheEncoding().build();

    public static void init(BiConsumer<ResourceLocation, DataComponentType<?>> r) {
        r.accept(id("wire_line_component"), WIRE_LINE_COMPONENT);
        r.accept(id("keyed_disc_component"), KEYED_DISC_COMPONENT);
    }

    private static ResourceLocation id(String path) {
        return HyphaPiracea.id(path);
    }
}
