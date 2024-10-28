package phanastrae.ywsanf.component.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipProvider;
import org.joml.Vector3f;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.util.CodecUtil;

import java.util.function.Consumer;

public class WireLineComponent implements TooltipProvider {
    public static final Codec<WireLineComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            CodecUtil.NON_NEGATIVE_FLOAT.fieldOf("max_wire_length").forGetter(WireLineComponent::maxWireLength),
                            CodecUtil.NON_NEGATIVE_FLOAT.fieldOf("range_of_influence").forGetter(WireLineComponent::rangeOfInfluence),
                            Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(WireLineComponent::showInTooltip),
                            ResourceLocation.CODEC.fieldOf("texture").forGetter(WireLineComponent::texture),
                            ExtraCodecs.VECTOR3F.fieldOf("light_color").forGetter(WireLineComponent::lightColor),
                            ExtraCodecs.VECTOR3F.fieldOf("dark_color").forGetter(WireLineComponent::darkColor)
                    )
                    .apply(instance, WireLineComponent::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, WireLineComponent> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            WireLineComponent::maxWireLength,
            ByteBufCodecs.FLOAT,
            WireLineComponent::rangeOfInfluence,
            ByteBufCodecs.BOOL,
            WireLineComponent::showInTooltip,
            ResourceLocation.STREAM_CODEC,
            WireLineComponent::texture,
            ByteBufCodecs.VECTOR3F,
            WireLineComponent::lightColor,
            ByteBufCodecs.VECTOR3F,
            WireLineComponent::darkColor,
            WireLineComponent::new
    );

    private final float maxWireLength;
    private final float rangeOfInfluence;
    private final boolean showInTooltip;
    private final ResourceLocation texture;
    private final ResourceLocation textureFull;
    private final Vector3f lightColor;
    private final Vector3f darkColor;

    public WireLineComponent(float maxWireLength, float rangeOfInfluence, boolean showInTooltip, ResourceLocation texture, Vector3f lightColor, Vector3f darkColor) {
        this.maxWireLength = maxWireLength;
        this.rangeOfInfluence = rangeOfInfluence;
        this.showInTooltip = showInTooltip;
        this.texture = texture;
        this.textureFull = texture.withPath(path -> "textures/" + path + ".png");
        this.lightColor = lightColor;
        this.darkColor = darkColor;
    }

    public WireLineComponent(float maxWireLength, float rangeOfInfluence, ResourceLocation texture, Vector3f lightColor, Vector3f darkColor) {
        this(maxWireLength, rangeOfInfluence, true, texture, lightColor, darkColor);
    }

    public WireLineComponent(float maxWireLength, float rangeOfInfluence) {
        this(maxWireLength, rangeOfInfluence, textureOf("hyphaline"), new Vector3f(0.7F, 0.7F, 0.5F), new Vector3f(0.49F, 0.49F, 0.35F));
    }

    public static ResourceLocation textureOf(String name) {
        return YWSaNF.id("entity/hyphal_coil/" + name);
    }

    public float maxWireLength() {
        return this.maxWireLength;
    }

    public float rangeOfInfluence() {
        return this.rangeOfInfluence;
    }

    public boolean showInTooltip() {
        return this.showInTooltip;
    }

    public ResourceLocation texture() {
        return this.texture;
    }

    public ResourceLocation getTextureFull() {
        return this.textureFull;
    }

    public Vector3f lightColor() {
        return this.lightColor;
    }

    public Vector3f darkColor() {
        return this.darkColor;
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        if(this.showInTooltip) {
            tooltipAdder.accept(CommonComponents.EMPTY);
            tooltipAdder.accept(Component.translatable("item.modifiers.ywsanf.wire_line").withStyle(ChatFormatting.GRAY));

            tooltipAdder.accept(
                    CommonComponents.space()
                            .append(
                                    Component.translatable(
                                            "item.modifiers.ywsanf.equals.m",
                                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(this.maxWireLength),
                                            Component.translatable("item.modifiers.ywsanf.wire_line.max_wire_length")
                                    )
                            )
                            .withStyle(ChatFormatting.AQUA)
            );

            tooltipAdder.accept(
                    CommonComponents.space()
                            .append(
                                    Component.translatable(
                                            "item.modifiers.ywsanf.equals.m",
                                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(this.rangeOfInfluence),
                                            Component.translatable("item.modifiers.ywsanf.wire_line.radius_of_influence")
                                    )
                            )
                            .withStyle(ChatFormatting.AQUA)
            );
        }
    }
}
