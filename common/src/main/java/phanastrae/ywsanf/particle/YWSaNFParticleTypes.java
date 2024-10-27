package phanastrae.ywsanf.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.mixin.SimpleParticleTypeAccessor;

import java.util.function.BiConsumer;

public class YWSaNFParticleTypes {

    public static final SimpleParticleType ELECTROMAGNETIC_DUST = simple(false);
    public static final SimpleParticleType LINE_SPECK = simple(false);

    public static void init(BiConsumer<ResourceLocation, ParticleType<?>> r) {
        r.accept(id("electromagnetic_dust"), ELECTROMAGNETIC_DUST);
        r.accept(id("line_speck"), LINE_SPECK);
    }

    private static ResourceLocation id(String path) {
        return YWSaNF.id(path);
    }

    private static SimpleParticleType simple(boolean overrideLimiter) {
        return SimpleParticleTypeAccessor.invokeInit(overrideLimiter);
    }
}
