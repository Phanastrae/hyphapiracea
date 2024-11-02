package phanastrae.ywsanf.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public class FairyFogParticle extends TextureSheetParticle {
    protected float speedMultiplier = 1;

    FairyFogParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.setSize(0.02F, 0.02F);
        this.quadSize = this.quadSize * (this.random.nextFloat() * 1.9F + 0.7F);
        this.xd *= 0.02F;
        this.yd *= 0.02F;
        this.zd *= 0.02F;
        this.lifetime = (int)(20.0 / (Math.random() * 0.8 + 0.2));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.xd += (random.nextFloat() * 2.0 - 1.0) * 0.01 * speedMultiplier;
        this.yd += (random.nextFloat() * 2.0 - 1.0) * 0.01 * speedMultiplier;
        this.zd += (random.nextFloat() * 2.0 - 1.0) * 0.01 * speedMultiplier;

        this.yd += 0.005 * speedMultiplier;

        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.99;
            this.yd *= 0.99;
            this.zd *= 0.99;
        }
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        float relativeAge = ((float)this.age + scaleFactor) / (float)this.lifetime;
        return this.quadSize * relativeAge * (1.0F - relativeAge * relativeAge);
    }

    @Override
    protected int getLightColor(float partialTick) {
        int i = super.getLightColor(partialTick);
        int r = i & 0xFF;
        int b = (i >> 16) & 0xFF;

        if(r <= 240) {
            r = r + 80;
            if(r > 240) {
                r = 240;
            }
        }

        return r | (b << 16);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        protected final SpriteSet sprite;

        public Provider(SpriteSet sprites) {
            this.sprite = sprites;
        }

        public FairyFogParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FairyFogParticle fairyFogParticle = new FairyFogParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            fairyFogParticle.pickSprite(this.sprite);

            RandomSource random = level.getRandom();

            float f = random.nextFloat();
            boolean b = random.nextBoolean();
            if(b) {
                // lime
                fairyFogParticle.setColor(0.35F + 0.2F * f, 0.9F + 0.1F * f, 0.5F + 0.1F * f);
            } else {
                // pink
                fairyFogParticle.setColor(0.9F + 0.05F * f, 0.45F + 0.2F * f, 0.7F + 0.2F * f);
            }

            return fairyFogParticle;
        }
    }

    public static class LargeProvider extends Provider {

        public LargeProvider(SpriteSet sprites) {
            super(sprites);
        }

        @Nullable
        @Override
        public FairyFogParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FairyFogParticle fairyFogParticle = super.createParticle(type, level, x, y, z, xSpeed, ySpeed, z);
            if(fairyFogParticle != null) {
                fairyFogParticle.quadSize *= 12;
                fairyFogParticle.lifetime = (int) (fairyFogParticle.lifetime * 2.5);
                fairyFogParticle.speedMultiplier *= 3;
            }

            return fairyFogParticle;
        }
    }
}
