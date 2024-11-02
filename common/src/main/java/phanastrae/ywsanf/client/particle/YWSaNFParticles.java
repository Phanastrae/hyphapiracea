package phanastrae.ywsanf.client.particle;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import phanastrae.ywsanf.particle.YWSaNFParticleTypes;

public class YWSaNFParticles {

    public static void init(ClientParticleRegistrar r) {
        r.register(YWSaNFParticleTypes.ELECTROMAGNETIC_DUST, ElectromagneticDustParticle.Provider::new);
        r.register(YWSaNFParticleTypes.FAIRY_FOG, FairyFogParticle.Provider::new);
        r.register(YWSaNFParticleTypes.LARGE_ELECTROMAGNETIC_DUST, ElectromagneticDustParticle.LargeProvider::new);
        r.register(YWSaNFParticleTypes.LARGE_FAIRY_FOG, FairyFogParticle.LargeProvider::new);
        r.register(YWSaNFParticleTypes.LINE_SPECK, LineSpeckParticle.Provider::new);
        r.register(YWSaNFParticleTypes.PIRACITE_BUBBLE_POP, PiraciticBubblePopParticle.Provider::new);
        r.register(YWSaNFParticleTypes.ZAPPY_GRIT, ZappyGritParticle.Provider::new);
    }

    @FunctionalInterface
    public interface ClientParticleRegistrar {
        <T extends ParticleOptions> void register(ParticleType<T> type, ParticleRegistration<T> registration);
    }

    @FunctionalInterface
    public interface ParticleRegistration<T extends ParticleOptions> {
        ParticleProvider<T> create(SpriteSet sprites);
    }
}
