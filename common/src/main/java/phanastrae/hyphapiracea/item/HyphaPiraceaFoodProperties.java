package phanastrae.hyphapiracea.item;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import phanastrae.hyphapiracea.entity.status.HyphaPiraceaStatusEffects;

public class HyphaPiraceaFoodProperties {

    public static final FoodProperties POSITIVE_SPOREBERRY = getSporeberry(HyphaPiraceaStatusEffects.POSITIVELY_CHARGED_ENTRY);
    public static final FoodProperties NEGATIVE_SPOREBERRY = getSporeberry(HyphaPiraceaStatusEffects.NEGATIVELY_CHARGED_ENTRY);
    public static final FoodProperties NORTHERN_SPOREBERRY = getSporeberry(HyphaPiraceaStatusEffects.NORTHERLY_CHARGED_ENTRY);
    public static final FoodProperties SOUTHERN_SPOREBERRY = getSporeberry(HyphaPiraceaStatusEffects.SOUTHERLY_CHARGED_ENTRY);

    public static FoodProperties getSporeberry(Holder<MobEffect> effect) {
        return new FoodProperties.Builder()
                .nutrition(3)
                .saturationModifier(0.3F)
                .effect(new MobEffectInstance(effect, 200, 0), 1)
                .alwaysEdible()
                .fast()
                .build();
    }
}
