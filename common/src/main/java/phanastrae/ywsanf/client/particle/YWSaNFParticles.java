package phanastrae.ywsanf.client.particle;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import phanastrae.ywsanf.particle.YWSaNFParticleTypes;

public class YWSaNFParticles {

    public static void init(ClientParticleRegistrar r) {
        r.register(YWSaNFParticleTypes.ELECTROMAGNETIC_DUST, ElectromagneticDustParticle.Provider::new);
        r.register(YWSaNFParticleTypes.LINE_SPECK, LineSpeckParticle.Provider::new);
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
