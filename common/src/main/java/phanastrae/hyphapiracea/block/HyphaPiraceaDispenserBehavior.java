package phanastrae.hyphapiracea.block;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.DispenserBlock;
import phanastrae.hyphapiracea.item.HyphaPiraceaItems;

public class HyphaPiraceaDispenserBehavior {

    public static void init() {
        registerProjectileBehaviors(
                HyphaPiraceaItems.POSITIVE_CHARGEBALL,
                HyphaPiraceaItems.NEGATIVE_CHARGEBALL,
                HyphaPiraceaItems.NORTHERN_CHARGEBALL,
                HyphaPiraceaItems.SOUTHERN_CHARGEBALL);
    }

    public static void registerProjectileBehaviors(ItemLike... items) {
        for(ItemLike item : items) {
            DispenserBlock.registerProjectileBehavior(item);
        }
    }
}
