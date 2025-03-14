package phanastrae.hyphapiracea.component.type;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import phanastrae.hyphapiracea.component.HyphaPiraceaComponentTypes;

import java.util.function.Consumer;

public record DiscLockComponent(String discLock, boolean showInTooltip) implements TooltipProvider {
    public static final Codec<DiscLockComponent> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.STRING.fieldOf("disc_lock").forGetter(DiscLockComponent::discLock),
                            Codec.BOOL.optionalFieldOf("show_in_tooltip", true).forGetter(DiscLockComponent::showInTooltip)
                    )
                    .apply(instance, DiscLockComponent::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DiscLockComponent> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(Codec.STRING),
            DiscLockComponent::discLock,
            ByteBufCodecs.BOOL,
            DiscLockComponent::showInTooltip,
            DiscLockComponent::new
    );

    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(o instanceof DiscLockComponent other) {
            return this.discLock.equals(other.discLock)
                    && (this.showInTooltip == other.showInTooltip);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.discLock.hashCode()
                ^ (this.showInTooltip ? 12345 : 56789);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        if(this.showInTooltip) {
            tooltipAdder.accept(CommonComponents.EMPTY);
            tooltipAdder.accept(Component.translatable("item.modifiers.hyphapiracea.keyed_disc.lock", this.discLock).withStyle(ChatFormatting.RED));
        }
    }

    public static String getDiscLockFromDisc(ItemStack disc) {
        if(disc.has(HyphaPiraceaComponentTypes.DISC_LOCK_COMPONENT)) {
            return disc.get(HyphaPiraceaComponentTypes.DISC_LOCK_COMPONENT).discLock();
        } else {
            return "";
        }
    }

    public static String getDiscLockFromLock(ItemStack lock) {
        if (lock.has(DataComponents.CUSTOM_NAME)) {
            Component name = lock.get(DataComponents.CUSTOM_NAME);
            return name.getString();
        } else {
            return "";
        }
    }
}
