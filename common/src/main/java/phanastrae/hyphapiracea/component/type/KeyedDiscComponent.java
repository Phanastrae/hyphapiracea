package phanastrae.hyphapiracea.component.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipProvider;
import phanastrae.hyphapiracea.structure.StructureType;
import phanastrae.hyphapiracea.util.CodecUtil;

import java.util.function.Consumer;

public record KeyedDiscComponent(ResourceLocation structureId, float maxOperatingRadius, float minOperatingTesla, float requiredPower, boolean showInTooltip, StructureType structureType, boolean rotateStructure) implements TooltipProvider {
    public static final StringRepresentable.StringRepresentableCodec<StructureType> STRUCTURE_TYPE_CODEC = StringRepresentable.fromEnum(StructureType::values);
    public static final StreamCodec<ByteBuf, StructureType> STRUCTURE_TYPE_STREAM_CODEC = ByteBufCodecs.fromCodec(STRUCTURE_TYPE_CODEC);

    public static final Codec<KeyedDiscComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            ResourceLocation.CODEC.fieldOf("structure_id").forGetter(KeyedDiscComponent::structureId),
                            ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("max_operating_radius", 16F).forGetter(KeyedDiscComponent::maxOperatingRadius),
                            CodecUtil.NON_NEGATIVE_FLOAT.optionalFieldOf("min_operating_tesla", 0.000001F).forGetter(KeyedDiscComponent::minOperatingTesla),
                            Codec.FLOAT.optionalFieldOf("required_power", 1F).forGetter(KeyedDiscComponent::requiredPower),
                            Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(KeyedDiscComponent::showInTooltip),
                            STRUCTURE_TYPE_CODEC.optionalFieldOf("type", StructureType.STRUCTURE).forGetter(KeyedDiscComponent::structureType),
                            Codec.BOOL.optionalFieldOf("rotate", true).forGetter(KeyedDiscComponent::rotateStructure)
                    )
                    .apply(instance, KeyedDiscComponent::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, KeyedDiscComponent> PACKET_CODEC = new StreamCodec<>() {
        @Override
        public KeyedDiscComponent decode(RegistryFriendlyByteBuf buf) {
            return new KeyedDiscComponent(
                    ResourceLocation.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    STRUCTURE_TYPE_STREAM_CODEC.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            );
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, KeyedDiscComponent component) {
            ResourceLocation.STREAM_CODEC.encode(buf, component.structureId);
            ByteBufCodecs.FLOAT.encode(buf, component.maxOperatingRadius);
            ByteBufCodecs.FLOAT.encode(buf, component.minOperatingTesla);
            ByteBufCodecs.FLOAT.encode(buf, component.requiredPower);
            ByteBufCodecs.BOOL.encode(buf, component.showInTooltip);
            STRUCTURE_TYPE_STREAM_CODEC.encode(buf, component.structureType);
            ByteBufCodecs.BOOL.encode(buf, component.rotateStructure);
        }
    };

    public KeyedDiscComponent(ResourceLocation structureId, float maxOperatingRadius, float minOperatingTesla, float requiredPower) {
        this(structureId, maxOperatingRadius, minOperatingTesla, requiredPower, true, StructureType.STRUCTURE, true);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof KeyedDiscComponent other) {
            return this.structureId.equals(other.structureId)
                    && this.maxOperatingRadius == other.maxOperatingRadius
                    && this.minOperatingTesla == other.minOperatingTesla
                    && this.requiredPower == other.requiredPower
                    && this.showInTooltip == other.showInTooltip
                    && this.structureType == other.structureType
                    && this.rotateStructure == other.rotateStructure;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.structureId.hashCode()
                ^ Float.floatToIntBits(this.maxOperatingRadius)
                ^ Float.floatToIntBits(this.minOperatingTesla + 1.23F)
                ^ Float.floatToIntBits(this.requiredPower + 4.56F)
                ^ (this.showInTooltip ? 0x87654321 : 12345678)
                ^ this.structureType.hashCode()
                ^ (this.rotateStructure ? 0x876543 : 0x98765);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        if(this.showInTooltip) {
            tooltipAdder.accept(CommonComponents.EMPTY);
            tooltipAdder.accept(Component.translatable("item.modifiers.hyphapiracea.keyed_disc").withStyle(ChatFormatting.GRAY));

            String idKey = this.structureType == StructureType.STRUCTURE ? "item.modifiers.hyphapiracea.keyed_disc.structure_id" : "item.modifiers.hyphapiracea.keyed_disc.template_id";
            tooltipAdder.accept(
                    CommonComponents.space()
                            .append(
                                    Component.translatable(
                                            idKey,
                                            this.structureId.toString()
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

            tooltipAdder.accept(
                    CommonComponents.space()
                            .append(
                                    Component.translatable(
                                            "item.modifiers.hyphapiracea.equals.watt",
                                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(this.requiredPower),
                                            Component.translatable("item.modifiers.hyphapiracea.keyed_disc.required_power")
                                    )
                            )
                            .withStyle(ChatFormatting.AQUA)
            );
        }
    }
}
