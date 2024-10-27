package phanastrae.ywsanf.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.ywsanf.duck.LevelDuckInterface;
import phanastrae.ywsanf.world.YWSaNFLevelAttachment;

import java.util.function.Supplier;

@Mixin(Level.class)
public class LevelMixin implements LevelDuckInterface {

    @Unique
    private YWSaNFLevelAttachment ywsanf$attachment;

    @Override
    public YWSaNFLevelAttachment ywsanf$getAttachment() {
        return ywsanf$attachment;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ywsanf$init(WritableLevelData levelData, ResourceKey dimension, RegistryAccess registryAccess, Holder dimensionTypeRegistration, Supplier profiler, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates, CallbackInfo ci) {
        this.ywsanf$attachment = new YWSaNFLevelAttachment((Level)(Object)this);
    }
}
