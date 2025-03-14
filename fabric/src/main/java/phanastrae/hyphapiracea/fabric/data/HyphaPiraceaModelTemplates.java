package phanastrae.hyphapiracea.fabric.data;

import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import phanastrae.hyphapiracea.HyphaPiracea;

import java.util.Optional;

public class HyphaPiraceaModelTemplates {
    public static final TextureSlot TINT_SIDE = TextureSlot.create("tint_side");
    public static final TextureSlot BASE_SIDE = TextureSlot.create("base_side");
    public static final TextureSlot BASE_END = TextureSlot.create("base_end");
    public static final TextureSlot ROD_SIDE = TextureSlot.create("rod_side");
    public static final TextureSlot ROD_END = TextureSlot.create("rod_end");

    public static final ModelTemplate CONDUCTOR = create(
            blockId("template_conductor"), TextureSlot.PARTICLE, BASE_SIDE, BASE_END, ROD_SIDE, ROD_END
    );
    public static final ModelTemplate CONDUCTOR_WALL = create(
            blockId("template_conductor_wall"), "_wall", TextureSlot.PARTICLE, BASE_SIDE, BASE_END, ROD_SIDE, ROD_END
    );
    public static final ModelTemplate CUBE_BOTTOM_TOP_TINTED_SIDES = create(
            blockId("cube_bottom_top_tinted_sides"), TextureSlot.PARTICLE, TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE, TINT_SIDE
    );
    public static final ModelTemplate SINGLE_FACE_WITH_INDICATORS = create(
            blockId("template_single_face_indicators"), TextureSlot.PARTICLE, TextureSlot.SIDE, TextureSlot.BACK
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
