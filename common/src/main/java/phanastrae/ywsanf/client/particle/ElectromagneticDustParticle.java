package phanastrae.ywsanf.client.particle;


import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import phanastrae.ywsanf.world.YWSaNFLevelAttachment;

public class ElectromagneticDustParticle extends TextureSheetParticle {
    public static final int TRAIL_LENGTH = 7;

    protected final ClientLevel level;

    protected float accelerationDampening;
    protected float electricCharge;
    protected float magneticCharge;
    protected float mass;

    protected double[] xos;
    protected double[] yos;
    protected double[] zos;

    protected TextureAtlasSprite[] sprites;

    public ElectromagneticDustParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.yd = this.yd - 0.1F; // remove bias

        this.level = level;
        this.lifetime *= 5;
        this.friction = 0.99f;
        this.accelerationDampening = 1;

        this.xos = new double[TRAIL_LENGTH];
        this.yos = new double[TRAIL_LENGTH];
        this.zos = new double[TRAIL_LENGTH];
        this.sprites = new TextureAtlasSprite[TRAIL_LENGTH + 1];
    }

    public void setElectricCharge(float charge) {
        this.electricCharge = charge;
    }

    public void setMagneticCharge(float charge) {
        this.magneticCharge = charge;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.accelerationDampening *= 0.9f;

        Vec3 position = new Vec3(this.x, this.y, this.z);
        Vec3 velocity = new Vec3(this.xd * 20, this.yd * 20, this.zd * 20);

        Vec3 magneticField = YWSaNFLevelAttachment.getAttachment(this.level).getMagneticFieldAtPosition(position);
        Vec3 eForce = this.electricCharge == 0 ? Vec3.ZERO : velocity.cross(magneticField).scale(this.electricCharge);
        Vec3 mForce = this.magneticCharge == 0 ? Vec3.ZERO : magneticField.scale(this.magneticCharge);

        Vec3 totalForce = eForce.add(mForce);
        // divide force by mass to get acceleration (meters per second squared)
        // then divide by 400 to get acceleration (meters per tick squared)
        // also multiply by (1 - accelerationDampnening) to slowly increase acceleration over time
        Vec3 pdd = totalForce.scale((1f - this.accelerationDampening) / (this.mass * 400));

        this.setParticleSpeed(this.xd + pdd.x, this.yd + pdd.y, this.zd + pdd.z);

        for(int i = TRAIL_LENGTH - 1; i > 0; i--) {
            this.xos[i] = this.xos[i-1];
            this.yos[i] = this.yos[i-1];
            this.zos[i] = this.zos[i-1];
        }
        this.xos[0] = this.xo;
        this.yos[0] = this.yo;
        this.zos[0] = this.zo;
        super.tick();
    }

    @Override
    public void move(double x, double y, double z) {
        this.setBoundingBox(this.getBoundingBox().move(x, y, z));
        this.setLocationFromBoundingbox();
    }

    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        Quaternionf quaternionf = new Quaternionf();
        this.getFacingCameraMode().setRotation(quaternionf, renderInfo, partialTicks);
        if (this.roll != 0.0F) {
            quaternionf.rotateZ(Mth.lerp(partialTicks, this.oRoll, this.roll));
        }

        Vec3 vec3 = renderInfo.getPosition();
        double cx = vec3.x();
        double cy = vec3.y();
        double cz = vec3.z();
        int lightColor = this.getLightColor(partialTicks);
        float quadSize = this.getQuadSize(partialTicks);
        renderQuad(buffer, quaternionf, partialTicks, lightColor, quadSize, 0, this.x, this.y, this.z, this.xo, this.yo, this.zo, cx, cy, cz);
        renderQuad(buffer, quaternionf, partialTicks, lightColor, quadSize, 1, this.xo, this.yo, this.zo, this.xos[0], this.yos[0], this.zos[0], cx, cy, cz);
        for(int i = 0; i < TRAIL_LENGTH - 1; i++) {
            renderQuad(buffer, quaternionf, partialTicks, lightColor, quadSize, i + 2, this.xos[i], this.yos[i], this.zos[i], this.xos[i + 1], this.yos[i + 1], this.zos[i + 1], cx, cy, cz);
        }
    }

    public void renderQuad(VertexConsumer buffer, Quaternionf quaternion, float partialTicks, int lightColor, float quadSize, int i, double x, double y, double z, double xo, double yo, double zo, double cx, double cy, double cz) {
        float lx = (float)(Mth.lerp(partialTicks, xo, x) - cx);
        float ly = (float)(Mth.lerp(partialTicks, yo, y) - cy);
        float lz = (float)(Mth.lerp(partialTicks, zo, z) - cz);

        float adjustedQuadSize = quadSize * (TRAIL_LENGTH + 1F - i) / (TRAIL_LENGTH + 1F);

        TextureAtlasSprite sprite = this.sprites[i];
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        this.renderVertex(buffer, quaternion, lx, ly, lz, 1.0F, -1.0F, adjustedQuadSize, u1, v1, lightColor);
        this.renderVertex(buffer, quaternion, lx, ly, lz, 1.0F, 1.0F, adjustedQuadSize, u1, v0, lightColor);
        this.renderVertex(buffer, quaternion, lx, ly, lz, -1.0F, 1.0F, adjustedQuadSize, u0, v0, lightColor);
        this.renderVertex(buffer, quaternion, lx, ly, lz, -1.0F, -1.0F, adjustedQuadSize, u0, v1, lightColor);
    }

    protected void renderVertex(
            VertexConsumer buffer,
            Quaternionf quaternion,
            float x,
            float y,
            float z,
            float xOffset,
            float yOffset,
            float quadSize,
            float u,
            float v,
            int packedLight
    ) {
        Vector3f vector3f = new Vector3f(xOffset, yOffset, 0.0F).rotate(quaternion).mul(quadSize).add(x, y, z);
        buffer.addVertex(vector3f.x(), vector3f.y(), vector3f.z())
                .setUv(u, v)
                .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
                .setLight(packedLight);
    }

    @Override
    public void pickSprite(SpriteSet sprite) {
        this.setSprite(sprite.get(this.random));
        int l = this.sprites.length;
        for(int i = 0; i < l; i++) {
            this.sprites[i] = sprite.get(i, l - 1);
        }
    }

    @Override
    public float getQuadSize(float scaleFactor) {
        float relativeAge = ((float)this.age + scaleFactor) / (float)this.lifetime;
        relativeAge = Math.clamp(relativeAge, 0f, 1f);
        return this.quadSize * (1.0F - relativeAge * relativeAge);
    }

    @Override
    protected int getLightColor(float partialTick) {
        int i = super.getLightColor(partialTick);
        int r = i & 0xFF;
        int b = (i >> 16) & 0xFF;

        if(r <= 200) {
            r = r + 150;
            if(r > 200) {
                r = 200;
            }
        }

        return r | (b << 16);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet sprites) {
            this.sprite = sprites;
        }

        @Nullable
        @Override
        public ElectromagneticDustParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ElectromagneticDustParticle emDustParticle = new ElectromagneticDustParticle(level, x, y, z, xSpeed, ySpeed, zSpeed);
            emDustParticle.pickSprite(this.sprite);

            int i = level.random.nextInt(4);
            boolean chargeIsMagnetic = (i & 0x1) == 0;
            boolean positiveCharge = (i & 0x2) == 0;
            int chargeSign = positiveCharge ? 1 : -1;

            if(chargeIsMagnetic) {
                emDustParticle.setMagneticCharge(chargeSign * 1000);
                if(positiveCharge) {
                    // green
                    emDustParticle.setColor(0.5f, 1.0f, 0.25f);
                } else {
                    // magenta
                    emDustParticle.setColor(1.0f, 0.25f, 1.0f);
                }
            } else {
                emDustParticle.setElectricCharge(chargeSign * 600);
                if(positiveCharge) {
                    // red
                    emDustParticle.setColor(1.0f, 0.25f, 0.25f);
                } else {
                    // blue
                    emDustParticle.setColor(0.25f, 0.5f, 1.0f);
                }
            }
            emDustParticle.setMass(0.0001F);

            return emDustParticle;
        }
    }

    public static class LargeProvider extends ElectromagneticDustParticle.Provider {

        public LargeProvider(SpriteSet sprites) {
            super(sprites);
        }

        @Nullable
        @Override
        public ElectromagneticDustParticle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            ElectromagneticDustParticle electromagneticDustParticle = super.createParticle(type, level, x, y, z, xSpeed, ySpeed, z);
            if(electromagneticDustParticle != null) {
                electromagneticDustParticle.quadSize *= 4;
                electromagneticDustParticle.lifetime = electromagneticDustParticle.lifetime * 2;
                electromagneticDustParticle.mass *= 3;
            }

            return electromagneticDustParticle;
        }
    }
}