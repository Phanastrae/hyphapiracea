package phanastrae.ywsanf.component;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.component.type.WireLineComponent;

import java.util.function.BiConsumer;

public class YWSaNFComponentTypes {

    public static final DataComponentType<WireLineComponent> WIRE_LINE_COMPONENT =
            DataComponentType.<WireLineComponent>builder().persistent(WireLineComponent.CODEC).networkSynchronized(WireLineComponent.PACKET_CODEC).cacheEncoding().build();

    public static void init(BiConsumer<ResourceLocation, DataComponentType<?>> r) {
        r.accept(id("wire_line_component"), WIRE_LINE_COMPONENT);
    }

    private static ResourceLocation id(String path) {
        return YWSaNF.id(path);
    }
}
