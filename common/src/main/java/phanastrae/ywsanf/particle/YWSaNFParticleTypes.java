package phanastrae.ywsanf.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.mixin.SimpleParticleTypeAccessor;

import java.util.function.BiConsumer;

public class YWSaNFParticleTypes {

    public static final SimpleParticleType ELECTROMAGNETIC_DUST = simple(false);
    public static final SimpleParticleType FAIRY_FOG = simple(false);
    public static final SimpleParticleType LARGE_ELECTROMAGNETIC_DUST = simple(false);
    public static final SimpleParticleType LARGE_FAIRY_FOG = simple(false);
    public static final SimpleParticleType LINE_SPECK = simple(false);
    public static final SimpleParticleType PIRACITE_BUBBLE_POP = simple(false);
    public static final SimpleParticleType ZAPPY_GRIT = simple(false);

    public static void init(BiConsumer<ResourceLocation, ParticleType<?>> r) {
        r.accept(id("electromagnetic_dust"), ELECTROMAGNETIC_DUST);
        r.accept(id("large_electromagnetic_dust"), LARGE_ELECTROMAGNETIC_DUST);
        r.accept(id("fairy_fog"), FAIRY_FOG);
        r.accept(id("large_fairy_fog"), LARGE_FAIRY_FOG);
        r.accept(id("line_speck"), LINE_SPECK);
        r.accept(id("piracite_bubble_pop"), PIRACITE_BUBBLE_POP);
        r.accept(id("zappy_grit"), ZAPPY_GRIT);
    }

    private static ResourceLocation id(String path) {
        return YWSaNF.id(path);
    }

    private static SimpleParticleType simple(boolean overrideLimiter) {
        return SimpleParticleTypeAccessor.invokeInit(overrideLimiter);
    }
}
