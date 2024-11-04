package phanastrae.hyphapiracea.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.HyphaPiracea;

public class HyphaPiraceaDamageTypes {

    public static ResourceKey<DamageType> CHARGEBALL = ResourceKey.create(Registries.DAMAGE_TYPE, HyphaPiracea.id("chargeball"));

    public static DamageSource of(Level level, ResourceKey<DamageType> key) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key));
    }

    public static DamageSource of(Level level, ResourceKey<DamageType> key, @Nullable Entity causingEntity, @Nullable Entity directEntity) {
        return new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key), causingEntity, directEntity);
    }
}
