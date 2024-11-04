package phanastrae.hyphapiracea.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.EntityTypeTags;
import phanastrae.hyphapiracea.entity.HyphaPiraceaEntityTypes;

import java.util.concurrent.CompletableFuture;

public class EntityTypeTagProvider extends FabricTagProvider.EntityTypeTagProvider {
    public EntityTypeTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        getOrCreateTagBuilder(EntityTypeTags.IMPACT_PROJECTILES)
                .add(HyphaPiraceaEntityTypes.CHARGEBALL);

        getOrCreateTagBuilder(EntityTypeTags.REDIRECTABLE_PROJECTILE)
                .add(HyphaPiraceaEntityTypes.CHARGEBALL);
    }
}
