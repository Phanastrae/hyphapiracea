package phanastrae.hyphapiracea.fabric.data;

import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import phanastrae.hyphapiracea.HyphaPiracea;

import java.util.Optional;

public class HyphaPiraceaModelTemplates {
    public static final TextureSlot TINT_SIDE = TextureSlot.create("tint_side");

    public static final ModelTemplate CONDUCTOR = create(
            blockId("template_conductor"), TextureSlot.PARTICLE, TextureSlot.ALL
            );
    public static final ModelTemplate CONDUCTOR_WALL = create(
            blockId("template_conductor_wall"), "_wall", TextureSlot.PARTICLE, TextureSlot.ALL
    );
    public static final ModelTemplate CUBE_BOTTOM_TOP_TINTED_SIDES = create(
            blockId("cube_bottom_top_tinted_sides"), TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE, TINT_SIDE
    );

    public static ResourceLocation blockId(String location) {
        return HyphaPiracea.id("block/" + location);
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
