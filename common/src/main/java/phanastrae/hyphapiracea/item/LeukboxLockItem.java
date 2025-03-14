package phanastrae.hyphapiracea.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import phanastrae.hyphapiracea.component.HyphaPiraceaComponentTypes;
import phanastrae.hyphapiracea.component.type.DiscLockComponent;

public class LeukboxLockItem extends Item {
    public LeukboxLockItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if(!player.getAbilities().mayBuild) {
            return false;
        }

        if(action != ClickAction.SECONDARY) {
            return false;
        } else {
            String lockLock = DiscLockComponent.getDiscLockFromLock(stack);

            ItemStack disc = slot.getItem();
            if(disc.is(HyphaPiraceaItems.KEYED_DISC)) {
                String discLock = DiscLockComponent.getDiscLockFromDisc(disc);
                if(!lockLock.equals(discLock)) {
                    ItemStack newDisc = disc.copy();
                    if(lockLock.isEmpty()) {
                        newDisc.remove(HyphaPiraceaComponentTypes.DISC_LOCK_COMPONENT);
                    } else {
                        newDisc.set(HyphaPiraceaComponentTypes.DISC_LOCK_COMPONENT, new DiscLockComponent(lockLock, true));
                    }
                    slot.set(newDisc);

                    playLockSound(player);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    public static void playLockSound(Entity entity) {
        entity.playSound(SoundEvents.CHEST_LOCKED, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }
}
