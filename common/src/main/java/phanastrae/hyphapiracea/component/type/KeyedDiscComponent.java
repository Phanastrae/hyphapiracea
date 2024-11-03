package phanastrae.hyphapiracea.component.type;

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
import phanastrae.hyphapiracea.util.CodecUtil;

import java.util.function.Consumer;

public record KeyedDiscComponent(ResourceLocation structureId, float maxOperatingRadius, float minOperatingTesla, boolean showInTooltip) implements TooltipProvider {
    public static final Codec<KeyedDiscComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            ResourceLocation.CODEC.fieldOf("structure_id").forGetter(KeyedDiscComponent::structureId),
                            ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("max_operating_radius", 16F).forGetter(KeyedDiscComponent::maxOperatingRadius),
                            CodecUtil.NON_NEGATIVE_FLOAT.optionalFieldOf("min_operating_tesla", 0.000001F).forGetter(KeyedDiscComponent::minOperatingTesla),
                            Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(KeyedDiscComponent::showInTooltip)
                    )
                    .apply(instance, KeyedDiscComponent::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, KeyedDiscComponent> PACKET_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            KeyedDiscComponent::structureId,
            ByteBufCodecs.FLOAT,
            KeyedDiscComponent::maxOperatingRadius,
            ByteBufCodecs.FLOAT,
            KeyedDiscComponent::minOperatingTesla,
            ByteBufCodecs.BOOL,
            KeyedDiscComponent::showInTooltip,
            KeyedDiscComponent::new
    );

    public KeyedDiscComponent(ResourceLocation structureId, float maxOperatingRadius, float minOperatingTesla) {
        this(structureId, maxOperatingRadius, minOperatingTesla, true);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        if(this.showInTooltip) {
            tooltipAdder.accept(CommonComponents.EMPTY);
            tooltipAdder.accept(Component.translatable("item.modifiers.hyphapiracea.keyed_disc").withStyle(ChatFormatting.GRAY));

            tooltipAdder.accept(
                    CommonComponents.space()
                            .append(
                                    Component.translatable(
                                            "item.modifiers.hyphapiracea.keyed_disc.structure_id",
                                            this.structureId
                                    )
                            )
                            .withStyle(ChatFormatting.YELLOW)
            );

            tooltipAdder.accept(
                    CommonComponents.space()
                            .append(
                                    Component.translatable(
                                            "item.modifiers.hyphapiracea.equals.m",
                                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(this.maxOperatingRadius),
                                            Component.translatable("item.modifiers.hyphapiracea.keyed_disc.max_operating_radius")
                                    )
                            )
                            .withStyle(ChatFormatting.AQUA)
            );

            tooltipAdder.accept(
                    CommonComponents.space()
                            .append(
                                    Component.translatable(
                                            "item.modifiers.hyphapiracea.equals.microtesla",
                                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(this.minOperatingTesla * 1000000),
                                            Component.translatable("item.modifiers.hyphapiracea.keyed_disc.min_operating_tesla")
                                    )
                            )
                            .withStyle(ChatFormatting.AQUA)
            );
        }
    }
}
