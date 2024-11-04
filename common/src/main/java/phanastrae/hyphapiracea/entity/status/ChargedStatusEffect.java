package phanastrae.hyphapiracea.entity.status;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import phanastrae.hyphapiracea.electromagnetism.Electromagnetism;
import phanastrae.hyphapiracea.particle.HyphaPiraceaParticleTypes;
import phanastrae.hyphapiracea.world.HyphaPiraceaLevelAttachment;

public class ChargedStatusEffect extends MobEffect {

    private final float electricCharge;
    private final float magneticCharge;

    protected ChargedStatusEffect(MobEffectCategory category, int color, float electricCharge, float magneticCharge) {
        super(category, color);

        this.electricCharge = electricCharge;
        this.magneticCharge = magneticCharge;
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if(livingEntity instanceof Player player && player.getAbilities().flying) {
            return true;
        } else {
            Vec3 velocity = livingEntity.getDeltaMovement().scale(20);
            Vec3 magneticField = HyphaPiraceaLevelAttachment.getAttachment(livingEntity.level()).getMagneticFieldAtPosition(livingEntity.position());

            // scale to more reasonable values
            double actualElectricCharge = electricCharge * 500;
            double actualMagneticCharge = magneticCharge * 0.01F;

            Vec3 totalForce = Electromagnetism.calculateForce(magneticField, velocity, actualElectricCharge, actualMagneticCharge);

            double force = totalForce.length();
            double maxForce = 1E-2;
            if (force > maxForce) {
                totalForce = totalForce.scale(maxForce / force);
            }

            double maxDeltaMovement = 0.1;
            totalForce = totalForce.scale(maxDeltaMovement / maxForce);
            livingEntity.addDeltaMovement(totalForce.scale(amplifier + 1));
            if (livingEntity.getDeltaMovement().y > 0) {
                livingEntity.resetFallDistance();
            }

            livingEntity.level().addParticle(HyphaPiraceaParticleTypes.ZAPPY_GRIT, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), 0.0, 0.0, 0.0);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
