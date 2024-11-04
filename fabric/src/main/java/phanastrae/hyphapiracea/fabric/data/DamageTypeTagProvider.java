package phanastrae.hyphapiracea.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import phanastrae.hyphapiracea.entity.HyphaPiraceaDamageTypes;

import java.util.concurrent.CompletableFuture;

public class DamageTypeTagProvider extends FabricTagProvider<DamageType> {

    public DamageTypeTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, Registries.DAMAGE_TYPE, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        getOrCreateTagBuilder(DamageTypeTags.IS_PROJECTILE)
                .addOptional(HyphaPiraceaDamageTypes.CHARGEBALL);
        getOrCreateTagBuilder(DamageTypeTags.IS_LIGHTNING)
                .addOptional(HyphaPiraceaDamageTypes.CHARGEBALL);
        getOrCreateTagBuilder(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS)
                .addOptional(HyphaPiraceaDamageTypes.CHARGEBALL);
        getOrCreateTagBuilder(DamageTypeTags.PANIC_CAUSES)
                .addOptional(HyphaPiraceaDamageTypes.CHARGEBALL);
    }
}
