package phanastrae.hyphapiracea.item;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import phanastrae.hyphapiracea.entity.status.HyphaPiraceaStatusEffects;

public class HyphaPiraceaFoodProperties {

    public static final FoodProperties POSITIVE_SPOREBERRY = getSporeberry(HyphaPiraceaStatusEffects.POSITIVELY_CHARGED_ENTRY);
    public static final FoodProperties NEGATIVE_SPOREBERRY = getSporeberry(HyphaPiraceaStatusEffects.NEGATIVELY_CHARGED_ENTRY);
    public static final FoodProperties NORTHERN_SPOREBERRY = getSporeberry(HyphaPiraceaStatusEffects.NORTHERLY_CHARGED_ENTRY);
    public static final FoodProperties SOUTHERN_SPOREBERRY = getSporeberry(HyphaPiraceaStatusEffects.SOUTHERLY_CHARGED_ENTRY);
    public static final FoodProperties PIRACEATIC_GLOB = new FoodProperties.Builder()
            .nutrition(7)
            .saturationModifier(1.3F)
            .effect(new MobEffectInstance(MobEffects.HARM, 1, 0), 1)
            .effect(new MobEffectInstance(MobEffects.WITHER, 200, 3), 1)
            .effect(new MobEffectInstance(MobEffects.BLINDNESS, 300, 0), 1)
            .effect(new MobEffectInstance(MobEffects.WEAKNESS, 500, 3), 1)
            .effect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 400, 3), 1)
            .effect(new MobEffectInstance(MobEffects.CONFUSION, 500, 0), 1)
            .effect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0), 1)
            .effect(new MobEffectInstance(MobEffects.LEVITATION, 60, 3), 1)
            .effect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 4), 1)
            .effect(new MobEffectInstance(MobEffects.JUMP, 300, 0), 1)
            .effect(new MobEffectInstance(MobEffects.LUCK, 500, 4), 1)
            .alwaysEdible()
            .build();

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
