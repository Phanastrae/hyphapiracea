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
            // scale to more reasonable values
            double actualElectricCharge = electricCharge * 500;
            double actualMagneticCharge = magneticCharge * 0.01F;

            Vec3 velocity = livingEntity.getDeltaMovement().scale(20.0);

            HyphaPiraceaLevelAttachment hpla = HyphaPiraceaLevelAttachment.getAttachment(livingEntity.level());
            double h = livingEntity.getBbHeight();
            double w = livingEntity.getBbWidth();
            Vec3 centerPos = livingEntity.position().add(0, h * 0.5, 0);

            Vec3 totalForce = Vec3.ZERO;
            // sample from the 8 corners of the player and get the average
            for(int i = 0; i < 8; i++) {
                boolean b1 = (i & 0x1) == 0;
                boolean b2 = (i & 0x2) == 0;
                boolean b3 = (i & 0x4) == 0;

                Vec3 pos = centerPos.add(
                        (b1 ? 1 : -1) * w * 0.5,
                        (b2 ? 1 : -1) * h * 0.5,
                        (b3 ? 1 : -1) * w * 0.5
                );

                Vec3 magneticField = hpla.getMagneticFieldAtPosition(pos);
                Vec3 force = Electromagnetism.calculateForce(magneticField, velocity, actualElectricCharge, actualMagneticCharge);
                totalForce = totalForce.add(force.scale(1.0 / 8));
            }

            double force = totalForce.length();
            double maxForce = 1E-2;
            if (force > maxForce) {
                totalForce = totalForce.scale(maxForce / force);
            }

            double maxDeltaMovement = 0.1;
            totalForce = totalForce.scale(maxDeltaMovement / maxForce);
            livingEntity.addDeltaMovement(totalForce.scale(amplifier + 1));

            Vec3 deltaMovement = livingEntity.getDeltaMovement();
            if (deltaMovement.y > 0) {
                // reset fall height if entity is moving upwards
                livingEntity.resetFallDistance();
            } else if(totalForce.y > 0) {
                // limit fall damage if entity is being pushed upwards
                double drag = 0.98F;
                double gravity = livingEntity.getGravity();
                float fallDistance = livingEntity.fallDistance;
                double v = deltaMovement.y;

                // t = log(1 - [d-1]v/[g*d]) / (d-1)
                // expected fall time (in ticks)
                double dragMinusOne = drag - 1;
                double expectedFallTime = Math.log(1 - dragMinusOne * v/(gravity * drag)) / dragMinusOne;

                // y = [g*d/(d-1)] * (t - e^([d-1]t) / (d-1) + 1 / (d-1))
                // expected fall distance (in meters)
                double expectedYChange = (gravity*drag/dragMinusOne) * (expectedFallTime - Math.exp(dragMinusOne * expectedFallTime) / dragMinusOne + 1 / dragMinusOne);
                double expectedFall = -expectedYChange;
                if(expectedFall < 0) {
                    expectedFall = 0;
                }

                if(expectedFall < fallDistance) {
                    livingEntity.fallDistance = (float)expectedFall;
                }
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
