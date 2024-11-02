package phanastrae.hyphapiracea.client.particle;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import phanastrae.hyphapiracea.particle.HyphaPiraceaParticleTypes;

public class HyphaPiraceaParticles {

    public static void init(ClientParticleRegistrar r) {
        r.register(HyphaPiraceaParticleTypes.ELECTROMAGNETIC_DUST, ElectromagneticDustParticle.Provider::new);
        r.register(HyphaPiraceaParticleTypes.FAIRY_FOG, FairyFogParticle.Provider::new);
        r.register(HyphaPiraceaParticleTypes.LARGE_ELECTROMAGNETIC_DUST, ElectromagneticDustParticle.LargeProvider::new);
        r.register(HyphaPiraceaParticleTypes.LARGE_FAIRY_FOG, FairyFogParticle.LargeProvider::new);
        r.register(HyphaPiraceaParticleTypes.LINE_SPECK, LineSpeckParticle.Provider::new);
        r.register(HyphaPiraceaParticleTypes.PIRACITE_BUBBLE_POP, PiraciticBubblePopParticle.Provider::new);
        r.register(HyphaPiraceaParticleTypes.ZAPPY_GRIT, ZappyGritParticle.Provider::new);
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
