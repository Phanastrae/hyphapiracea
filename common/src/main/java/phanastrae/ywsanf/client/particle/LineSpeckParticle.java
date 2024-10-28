package phanastrae.ywsanf.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class LineSpeckParticle extends TextureSheetParticle {
    LineSpeckParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.setSize(0.02F, 0.02F);
        this.quadSize = this.quadSize * (this.random.nextFloat() * 1.3F + 0.6F);
        this.xd *= 0.02F;
        this.yd *= 0.02F;
        this.zd *= 0.02F;
        this.lifetime = (int)(40.0 / (Math.random() * 0.8 + 0.2));
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
        if (this.lifetime-- <= 0) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.99;
            this.yd *= 0.99;
            this.zd *= 0.99;
        }
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
        private final SpriteSet sprite;

        public Provider(SpriteSet sprites) {
            this.sprite = sprites;
        }

        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            LineSpeckParticle lineSpeckParticle = new LineSpeckParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            lineSpeckParticle.pickSprite(this.sprite);

            RandomSource random = level.getRandom();

            float f = random.nextFloat() * 0.1F + 0.2F;
            int i = random.nextInt(3);
            if(i == 0) {
                // dark blue
                float g = f * 0.3F + 0.3F;
                lineSpeckParticle.setColor(0.2F + 0.3F * f, 0.2F + 0.3F * f, 0.3F + 0.4F * f);
            } else if(i == 1) {
                // pink
                lineSpeckParticle.setColor(0.8F + 0.2F * f, 0.4F + 0.3F * f, 0.5F + 0.4F * f);
            } else {
                // turquoise
                lineSpeckParticle.setColor(0.4F + 0.3F * f, 0.8F + 0.2F * f, 0.6F + 0.15F * f);
            }

            return lineSpeckParticle;
        }
    }
}
