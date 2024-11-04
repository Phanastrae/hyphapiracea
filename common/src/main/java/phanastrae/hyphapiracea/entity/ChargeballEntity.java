package phanastrae.hyphapiracea.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.electromagnetism.Electromagnetism;
import phanastrae.hyphapiracea.entity.status.HyphaPiraceaStatusEffects;
import phanastrae.hyphapiracea.particle.HyphaPiraceaParticleTypes;
import phanastrae.hyphapiracea.world.HyphaPiraceaLevelAttachment;

public class ChargeballEntity extends AbstractHurtingProjectile implements ItemSupplier {
    public static final String TAG_ELECTRIC_CHARGE = "electric_charge";
    public static final String TAG_MAGNETIC_CHARGE = "magnetic_charge";
    private static final EntityDataAccessor<Float> DATA_ELECTRIC_CHARGE = SynchedEntityData.defineId(ChargeballEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_MAGNETIC_CHARGE = SynchedEntityData.defineId(ChargeballEntity.class, EntityDataSerializers.FLOAT);

    public ChargeballEntity(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super(entityType, level);
        this.accelerationPower = 0.0;
    }

    public ChargeballEntity(Entity owner, Level level, double x, double y, double z) {
        super(HyphaPiraceaEntityTypes.CHARGEBALL, x, y, z, level);
        this.setOwner(owner);
        this.accelerationPower = 0.0;
    }

    public ChargeballEntity(Level level, double x, double y, double z, Vec3 movement) {
        super(HyphaPiraceaEntityTypes.CHARGEBALL, x, y, z, movement, level);
        this.accelerationPower = 0.0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ELECTRIC_CHARGE, 0F);
        builder.define(DATA_MAGNETIC_CHARGE, 0F);
    }

    public void setElectricCharge(float electricCharge) {
        this.entityData.set(DATA_ELECTRIC_CHARGE, electricCharge);
    }

    public void setMagneticCharge(float magneticCharge) {
        this.entityData.set(DATA_MAGNETIC_CHARGE, magneticCharge);
    }

    public float getElectricCharge() {
        return this.entityData.get(DATA_ELECTRIC_CHARGE);
    }

    public float getMagneticCharge() {
        return this.entityData.get(DATA_MAGNETIC_CHARGE);
    }

    public void setCharges(float electricCharge, float magneticCharge) {
        this.setElectricCharge(electricCharge);
        this.setMagneticCharge(magneticCharge);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat(TAG_ELECTRIC_CHARGE, this.getElectricCharge());
        compound.putFloat(TAG_MAGNETIC_CHARGE, this.getMagneticCharge());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if(compound.contains(TAG_ELECTRIC_CHARGE, CompoundTag.TAG_FLOAT)) {
            this.setElectricCharge(compound.getFloat(TAG_ELECTRIC_CHARGE));
        }
        if(compound.contains(TAG_MAGNETIC_CHARGE, CompoundTag.TAG_FLOAT)) {
            this.setMagneticCharge(compound.getFloat(TAG_MAGNETIC_CHARGE));
        }
    }

    @Override
    protected AABB makeBoundingBox() {
        float radius = this.getType().getDimensions().width() / 2.0F;
        float height = this.getType().getDimensions().height();
        float verticalOffset = 0.15F;
        return new AABB(
                this.position().x - radius,
                this.position().y - verticalOffset,
                this.position().z - radius,
                this.position().x + radius,
                this.position().y - verticalOffset + height,
                this.position().z + radius
        );
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        if(entity instanceof ChargeballEntity) {
            return false;
        } else {
            return super.canCollideWith(entity);
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (target instanceof ChargeballEntity) {
            return false;
        } else {
            return super.canHitEntity(target);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            LivingEntity livingentity = this.getOwner() instanceof LivingEntity livingentity1 ? livingentity1 : null;
            Entity entity = result.getEntity();
            if (livingentity != null) {
                livingentity.setLastHurtMob(entity);
            }

            if(entity instanceof LivingEntity target) {
                float electricCharge = this.getElectricCharge();
                float magneticCharge = this.getMagneticCharge();
                if(electricCharge > 0) {
                    addEffect(target, HyphaPiraceaStatusEffects.POSITIVELY_CHARGED_ENTRY, electricCharge);
                } else if(electricCharge < 0) {
                    addEffect(target, HyphaPiraceaStatusEffects.NEGATIVELY_CHARGED_ENTRY, -electricCharge);
                }
                if(magneticCharge > 0) {
                    addEffect(target, HyphaPiraceaStatusEffects.NORTHERLY_CHARGED_ENTRY, magneticCharge);
                } else if(magneticCharge < 0) {
                    addEffect(target, HyphaPiraceaStatusEffects.SOUTHERLY_CHARGED_ENTRY, -magneticCharge);
                }
            }

            DamageSource damageSource = HyphaPiraceaDamageTypes.of(this.level(), HyphaPiraceaDamageTypes.CHARGEBALL, this, livingentity);
            if (entity.hurt(damageSource, 1.0F) && entity instanceof LivingEntity livingentity2) {
                EnchantmentHelper.doPostAttackEffects((ServerLevel)this.level(), livingentity2, damageSource);
            }
        }
    }

    public void addEffect(LivingEntity livingEntity, Holder<MobEffect> effect, float absCharge) {
        int duration = Mth.ceil(absCharge * 200);
        livingEntity.addEffect(new MobEffectInstance(effect, duration, 0, true, true));
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    @Override
    protected float getInertia() {
        return 1.0F;
    }

    @Override
    protected float getLiquidInertia() {
        return this.getInertia();
    }

    @Nullable
    @Override
    protected ParticleOptions getTrailParticle() {
        return null;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.getBlockY() > this.level().getMaxBuildHeight() + 30) {
            this.discard();
        } else {
            Vec3 velocity = this.getDeltaMovement().scale(20);
            Vec3 magneticField = HyphaPiraceaLevelAttachment.getAttachment(this.level()).getMagneticFieldAtPosition(this.position());

            // scale to more reasonable values
            double actualElectricCharge = this.getElectricCharge() * 500;
            double actualMagneticCharge = this.getMagneticCharge() * 0.01F;

            Vec3 totalForce = Electromagnetism.calculateForce(magneticField, velocity, actualElectricCharge, actualMagneticCharge);

            double force = totalForce.length();
            double maxForce = 1E-2;
            if(force > maxForce) {
                totalForce = totalForce.scale(maxForce / force);
            }

            double maxDeltaMovement = 0.1;
            totalForce = totalForce.scale(maxDeltaMovement / maxForce);
            this.addDeltaMovement(totalForce);

            this.level().addParticle(HyphaPiraceaParticleTypes.ZAPPY_GRIT, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);

            super.tick();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }
}
