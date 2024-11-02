package phanastrae.ywsanf.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class PiraciticBubblePopParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    PiraciticBubblePopParticle(
            ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprite
    ) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.friction = 0.96F;
        this.sprites = sprite;
        this.scale(1.0F);
        this.hasPhysics = false;
        this.setSpriteFromAge(sprite);
        this.quadSize *= 3.5F;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 240;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public void setSpriteFromAge(SpriteSet sprite) {
        if (!this.removed) {
            float relativeAge = ((float)this.age) / (float)this.lifetime;
            int t = Mth.floor(Math.clamp((1 - (1 - relativeAge) * (1 - relativeAge)) * 12, 0, 10));
            this.setSprite(sprite.get(t,10));
        }
    }

    public record Provider(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed
        ) {
            PiraciticBubblePopParticle piraciticBubblePopParticle = new PiraciticBubblePopParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite);
            piraciticBubblePopParticle.setAlpha(1.0F);
            piraciticBubblePopParticle.setParticleSpeed(xSpeed, ySpeed, zSpeed);
            piraciticBubblePopParticle.setLifetime(level.random.nextInt(5) + 7);
            return piraciticBubblePopParticle;
        }
    }
}
