package phanastrae.hyphapiracea.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class ZappyGritParticle extends TextureSheetParticle {
    ZappyGritParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.setSize(0.02F, 0.02F);
        this.quadSize = this.quadSize * (this.random.nextFloat() * 1.3F + 0.6F);
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

        double a = this.age / (float)this.lifetime;

        double dx = ((random.nextFloat() * 2.0 - 1.0) * 0.02) * a * a;
        double dy = ((random.nextFloat() * 2.0 - 1.0) * 0.02) * a * a + 0.01 * a;
        double dz = ((random.nextFloat() * 2.0 - 1.0) * 0.02) * a * a;

        this.xd += ((random.nextFloat() * 2.0 - 1.0) * 0.03) * a * a;
        this.yd += ((random.nextFloat() * 2.0 - 1.0) * 0.03) * a * a + 0.003 * a;
        this.zd += ((random.nextFloat() * 2.0 - 1.0) * 0.03) * a * a;

        double oxd = this.xd;
        this.xd += this.zd * 0.08 * a;
        this.zd -= oxd * 0.08 * a;

        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.move(this.xd + dx, this.yd + dy, this.zd + dz);
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
            ZappyGritParticle zappyGritParticle = new ZappyGritParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            zappyGritParticle.pickSprite(this.sprite);

            RandomSource random = level.getRandom();

            float f = random.nextFloat();
            int i = random.nextInt(4);
            if(i == 0) {
                // lime
                zappyGritParticle.setColor(0.75F + 0.2F * f, 0.9F + 0.1F * f, 0.8F + 0.2F * f);
            } else if(i == 1) {
                // pink
                zappyGritParticle.setColor(0.9F + 0.1F * f, 0.75F + 0.2F * f, 0.8F + 0.2F * f);
            } else {
                // yellow, twice as likely
                zappyGritParticle.setColor(0.85F + 0.15F * f, 0.85F + 0.15F * f, 0.65F + 0.2F * f);
            }

            return zappyGritParticle;
        }
    }
}
