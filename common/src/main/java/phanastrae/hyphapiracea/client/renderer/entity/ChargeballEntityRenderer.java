package phanastrae.hyphapiracea.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.client.renderer.entity.model.ChargeBallModel;
import phanastrae.hyphapiracea.client.renderer.entity.model.HyphaPiraceaEntityModelLayers;
import phanastrae.hyphapiracea.entity.ChargeballEntity;

public class ChargeballEntityRenderer extends EntityRenderer<ChargeballEntity> {
    private static final float MIN_CAMERA_DISTANCE_SQUARED = Mth.square(3.5F);
    private static final ResourceLocation TEXTURE_LOCATION = HyphaPiracea.id("textures/entity/projectiles/chargeball.png");
    private final ChargeBallModel model;

    public ChargeballEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ChargeBallModel(context.bakeLayer(HyphaPiraceaEntityModelLayers.CHARGEBALL));
    }

    public void render(ChargeballEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        if (entity.tickCount >= 2 || !(this.entityRenderDispatcher.camera.getEntity().distanceToSqr(entity) < (double)MIN_CAMERA_DISTANCE_SQUARED)) {
            float f = (float)entity.tickCount + partialTick;
            VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.breezeWind(TEXTURE_LOCATION, this.xOffset(f) % 1.0F, 0.0F));
            this.model.setupAnim(entity, 0.0F, 0.0F, f, 0.0F, 0.0F);
            this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, this.getColor(entity));
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }

    public int getColor(ChargeballEntity entity) {
        float electricCharge = Math.clamp(entity.getElectricCharge(), -1, 1);
        float magneticCharge = Math.clamp(entity.getMagneticCharge(), -1, 1);

        Vec3 positive = new Vec3(0.9, 0.45, 0.45);
        Vec3 negative = new Vec3(0.5, 0.7, 0.9);
        Vec3 north = new Vec3(0.9, 0.3, 0.8);
        Vec3 south = new Vec3(0.5, 0.9, 0.6);

        Vec3 electricLerp = negative.lerp(positive, (electricCharge + 1) * 0.5);
        Vec3 magneticLerp = south.lerp(north, (magneticCharge + 1) * 0.5);

        float electricImportance = electricCharge * electricCharge - magneticCharge * magneticCharge;
        Vec3 color = magneticLerp.lerp(electricLerp, (electricImportance + 1) * 0.5);

        int r = (int)(color.x * 255) & 0xFF;
        int g = (int)(color.y * 255) & 0xFF;
        int b = (int)(color.z * 255) & 0xFF;
        return FastColor.ARGB32.color(255, r, g, b);
    }

    @Override
    protected int getBlockLightLevel(ChargeballEntity entity, BlockPos pos) {
        return Math.max(13, super.getBlockLightLevel(entity, pos));
    }

    protected float xOffset(float tickCount) {
        return tickCount * 0.03F;
    }

    @Override
    public ResourceLocation getTextureLocation(ChargeballEntity entity) {
        return TEXTURE_LOCATION;
    }
}
