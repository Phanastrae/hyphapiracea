package phanastrae.ywsanf.fabric.data;

import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import phanastrae.ywsanf.YWSaNF;

import java.util.Optional;

public class YWSaNFModelTemplates {
    public static final ModelTemplate CONDUCTOR = create(
            blockId("template_conductor"), TextureSlot.PARTICLE, TextureSlot.ALL
            );
    public static final ModelTemplate CONDUCTOR_WALL = create(
            blockId("template_conductor_wall"), "wall", TextureSlot.PARTICLE, TextureSlot.ALL
    );

    public static ResourceLocation blockId(String location) {
        return YWSaNF.id("block/" + location);
    }

    private static ModelTemplate create(ResourceLocation resourceLocation, TextureSlot... requiredSlots) {
        return new ModelTemplate(
                Optional.of(resourceLocation),
                Optional.empty(),
                requiredSlots
        );
    }

    private static ModelTemplate create(ResourceLocation resourceLocation, String suffix, TextureSlot... requiredSlots) {
        return new ModelTemplate(
                Optional.of(resourceLocation),
                Optional.of(suffix),
                requiredSlots
        );
    }
}
