package phanastrae.hyphapiracea.component.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
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
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.util.CodecUtil;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.Consumer;

public class WireLineComponent implements TooltipProvider {
    public static final Codec<WireLineComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            WireStats.CODEC.fieldOf("wire_stats").forGetter(WireLineComponent::wireStats),
                            WireVisuals.CODEC.fieldOf("wire_visuals").forGetter(WireLineComponent::wireVisuals),
                            Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(WireLineComponent::showInTooltip)
                    )
                    .apply(instance, WireLineComponent::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, WireLineComponent> PACKET_CODEC = StreamCodec.composite(
            WireStats.PACKET_CODEC,
            WireLineComponent::wireStats,
            WireVisuals.PACKET_CODEC,
            WireLineComponent::wireVisuals,
            ByteBufCodecs.BOOL,
            WireLineComponent::showInTooltip,
            WireLineComponent::new
    );

    public static final DecimalFormat FOUR_DIGIT_FORMAT = Util.make(
            new DecimalFormat("#.####"), format -> format.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT))
    );

    private final WireStats wireStats;
    private final WireVisuals wireVisuals;
    private final boolean showInTooltip;

    public WireLineComponent(WireStats wireStats, WireVisuals wireVisuals, boolean showInTooltip) {
        this.wireStats = wireStats;
        this.wireVisuals = wireVisuals;
        this.showInTooltip = showInTooltip;
    }

    public WireLineComponent(float maxWireLength, float rangeOfInfluence, float resistancePerBlock, float wardingRadius, boolean showInTooltip, ResourceLocation texture, Vector3f lightColor, Vector3f darkColor) {
        this.wireStats = new WireStats(maxWireLength, rangeOfInfluence, resistancePerBlock, wardingRadius);
        this.wireVisuals = new WireVisuals(texture, lightColor, darkColor);
        this.showInTooltip = showInTooltip;
    }

    public WireLineComponent(float maxWireLength, float rangeOfInfluence, float resistancePerBlock, float wardingRadius, ResourceLocation texture, Vector3f lightColor, Vector3f darkColor) {
        this(maxWireLength, rangeOfInfluence, resistancePerBlock, wardingRadius, true, texture, lightColor, darkColor);
    }

    public WireLineComponent(float maxWireLength, float rangeOfInfluence, float resistancePerBlock, float wardingRadius) {
        this(maxWireLength, rangeOfInfluence, resistancePerBlock, wardingRadius, textureOf("hyphaline"), new Vector3f(0.7F, 0.7F, 0.5F), new Vector3f(0.49F, 0.49F, 0.35F));
    }

    public static ResourceLocation textureOf(String name) {
        return HyphaPiracea.id("entity/hyphal_coil/" + name);
    }

    public WireStats wireStats() {
        return this.wireStats;
    }

    public WireVisuals wireVisuals() {
        return this.wireVisuals;
    }

    public boolean showInTooltip() {
        return this.showInTooltip;
    }

    public float maxWireLength() {
        return this.wireStats.maxWireLength;
    }

    public float rangeOfInfluence() {
        return this.wireStats.rangeOfInfluence;
    }

    public float wardingRadius() {
        return this.wireStats.wardingRadius;
    }

    public float resistancePerBlock() {
        return this.wireStats.resistancePerBlock;
    }

    public ResourceLocation texture() {
        return this.wireVisuals.texture;
    }

    public ResourceLocation getTextureFull() {
        return this.wireVisuals.textureFull;
    }

    public Vector3f lightColor() {
        return this.wireVisuals.lightColor;
    }

    public Vector3f darkColor() {
        return this.wireVisuals.darkColor;
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        if(this.showInTooltip) {
            tooltipAdder.accept(CommonComponents.EMPTY);
            tooltipAdder.accept(Component.translatable("item.modifiers.hyphapiracea.wire_line").withStyle(ChatFormatting.GRAY));

            if(this.wireStats.maxWireLength != 0) {
                tooltipAdder.accept(
                        CommonComponents.space()
                                .append(
                                        Component.translatable(
                                                "item.modifiers.hyphapiracea.equals.m",
                                                ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(this.wireStats.maxWireLength),
                                                Component.translatable("item.modifiers.hyphapiracea.wire_line.max_wire_length")
                                        )
                                )
                                .withStyle(ChatFormatting.AQUA)
                );
            }

            if(this.wireStats.rangeOfInfluence != 0) {
                tooltipAdder.accept(
                        CommonComponents.space()
                                .append(
                                        Component.translatable(
                                                "item.modifiers.hyphapiracea.equals.m",
                                                ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(this.wireStats.rangeOfInfluence),
                                                Component.translatable("item.modifiers.hyphapiracea.wire_line.radius_of_influence")
                                        )
                                )
                                .withStyle(ChatFormatting.AQUA)
                );
            }

            tooltipAdder.accept(
                    CommonComponents.space()
                            .append(
                                    Component.translatable(
                                            "item.modifiers.hyphapiracea.equals.ohm_per_meter",
                                            FOUR_DIGIT_FORMAT.format(this.wireStats.resistancePerBlock),
                                            Component.translatable("item.modifiers.hyphapiracea.wire_line.resistance_per_block")
                                    )
                            )
                            .withStyle(ChatFormatting.AQUA)
            );

            if(this.wireStats.wardingRadius != 0) {
                tooltipAdder.accept(
                        CommonComponents.space()
                                .append(
                                        Component.translatable(
                                                "item.modifiers.hyphapiracea.equals.m",
                                                ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(this.wireStats.wardingRadius),
                                                Component.translatable("item.modifiers.hyphapiracea.wire_line.warding_radius")
                                        )
                                )
                                .withStyle(ChatFormatting.AQUA)
                );
            }
        }
    }

    public record WireStats(float maxWireLength, float rangeOfInfluence, float resistancePerBlock, float wardingRadius) {
        public static final Codec<WireStats> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                CodecUtil.NON_NEGATIVE_FLOAT.fieldOf("max_wire_length").forGetter(WireStats::maxWireLength),
                                CodecUtil.NON_NEGATIVE_FLOAT.fieldOf("range_of_influence").forGetter(WireStats::rangeOfInfluence),
                                ExtraCodecs.POSITIVE_FLOAT.fieldOf("resistance_per_block").forGetter(WireStats::resistancePerBlock),
                                CodecUtil.NON_NEGATIVE_FLOAT.fieldOf("warding_radius").forGetter(WireStats::wardingRadius)
                        )
                        .apply(instance, WireStats::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, WireStats> PACKET_CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT,
                WireStats::maxWireLength,
                ByteBufCodecs.FLOAT,
                WireStats::rangeOfInfluence,
                ByteBufCodecs.FLOAT,
                WireStats::resistancePerBlock,
                ByteBufCodecs.FLOAT,
                WireStats::wardingRadius,
                WireStats::new
        );
    }

    public static class WireVisuals {
        public static final Codec<WireVisuals> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                ResourceLocation.CODEC.fieldOf("texture").forGetter(WireVisuals::texture),
                                ExtraCodecs.VECTOR3F.fieldOf("light_color").forGetter(WireVisuals::lightColor),
                                ExtraCodecs.VECTOR3F.fieldOf("dark_color").forGetter(WireVisuals::darkColor)
                        )
                        .apply(instance, WireVisuals::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, WireVisuals> PACKET_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC,
                WireVisuals::texture,
                ByteBufCodecs.VECTOR3F,
                WireVisuals::lightColor,
                ByteBufCodecs.VECTOR3F,
                WireVisuals::darkColor,
                WireVisuals::new
        );

        private final ResourceLocation texture;
        private final ResourceLocation textureFull;
        private final Vector3f lightColor;
        private final Vector3f darkColor;

        public WireVisuals(ResourceLocation texture, Vector3f lightColor, Vector3f darkColor) {
            this.texture = texture;
            this.textureFull = texture.withPath(path -> "textures/" + path + ".png");
            this.lightColor = lightColor;
            this.darkColor = darkColor;
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
    }
}
