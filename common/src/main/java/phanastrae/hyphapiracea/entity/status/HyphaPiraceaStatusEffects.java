package phanastrae.hyphapiracea.entity.status;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class HyphaPiraceaStatusEffects {

    public static final MobEffect POSITIVELY_CHARGED = new ChargedStatusEffect(MobEffectCategory.NEUTRAL, 0XFFFF9F9F, 1, 0);
    public static Holder<MobEffect> POSITIVELY_CHARGED_ENTRY;

    public static final MobEffect NEGATIVELY_CHARGED = new ChargedStatusEffect(MobEffectCategory.NEUTRAL, 0XFF9F9FFF, -1, 0);
    public static Holder<MobEffect> NEGATIVELY_CHARGED_ENTRY;

    public static final MobEffect NORTHERLY_CHARGED = new ChargedStatusEffect(MobEffectCategory.NEUTRAL, 0XFFFF9FFF, 0, 1);
    public static Holder<MobEffect> NORTHERLY_CHARGED_ENTRY;

    public static final MobEffect SOUTHERLY_CHARGED = new ChargedStatusEffect(MobEffectCategory.NEUTRAL, 0XFF9FFF9F, 0, -1);
    public static Holder<MobEffect> SOUTHERLY_CHARGED_ENTRY;

    public static void init(HolderRegisterHelper hrh) {
        POSITIVELY_CHARGED_ENTRY = hrh.register("positively_charged", POSITIVELY_CHARGED);
        NEGATIVELY_CHARGED_ENTRY = hrh.register("negatively_charged", NEGATIVELY_CHARGED);
        NORTHERLY_CHARGED_ENTRY = hrh.register("northerly_charged", NORTHERLY_CHARGED);
        SOUTHERLY_CHARGED_ENTRY = hrh.register("southerly_charged", SOUTHERLY_CHARGED);
    }

    @FunctionalInterface
    public interface HolderRegisterHelper {
        Holder<MobEffect> register(String name, MobEffect mobEffect);
    }
}
