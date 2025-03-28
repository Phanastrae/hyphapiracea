package phanastrae.hyphapiracea.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.world.HyphaPiraceaLevelAttachment;

public class LeyfieldEnvironmentEffects {
    private static final ResourceLocation LEYFIELD_AZIMUTH_SHIMMER = HyphaPiracea.id("textures/environment/leyfield_azimuth_shimmer.png");

    @Nullable
    private static VertexBuffer CYLINDER_BUFFER;

    private static float PREV_EFFECT_LEVEL;
    private static float EFFECT_LEVEL;

    public static void close() {
        closeIfNotNull(CYLINDER_BUFFER);
    }

    private static void closeIfNotNull(VertexBuffer vertexBuffer) {
        if(vertexBuffer != null) {
            vertexBuffer.close();
        }
    }

    private static void createCylinder() {
        closeIfNotNull(CYLINDER_BUFFER);

        CYLINDER_BUFFER = new VertexBuffer(VertexBuffer.Usage.STATIC);
        CYLINDER_BUFFER.bind();
        CYLINDER_BUFFER.upload(createCylinder(Tesselator.getInstance()));
        VertexBuffer.unbind();
    }

    private static MeshData createCylinder(Tesselator tessellator) {
        BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        float d = 100F;

        float yMax = 0.5F * d * Mth.PI * 0.25F;
        float yMin = -yMax;
        for(int i = 0; i < 64; i++) {
            float p0 = i / 64F;
            float p1 = (i+1) / 64F;

            float s0 = d * Mth.sin(p0 * Mth.TWO_PI);
            float c0 = d * Mth.cos(p0 * Mth.TWO_PI);
            float s1 = d * Mth.sin(p1 * Mth.TWO_PI);
            float c1 = d * Mth.cos(p1 * Mth.TWO_PI);

            bufferBuilder.addVertex(c0, yMax, s0).setUv(p0, 0.0F).setColor(1F, 1F, 1F, 1F);
            bufferBuilder.addVertex(c0, yMin, s0).setUv(p0, 1.0F).setColor(0.1F, 0.2F, 0.3F, 1F);
            bufferBuilder.addVertex(c1, yMin, s1).setUv(p1, 1.0F).setColor(0.1F, 0.2F, 0.3F, 1F);
            bufferBuilder.addVertex(c1, yMax, s1).setUv(p1, 0.0F).setColor(1F, 1F, 1F, 1F);
        }

        return bufferBuilder.buildOrThrow();
    }

    public static void renderSky(Matrix4f positionMatrix, DeltaTracker deltaTracker, ClientLevel level, Matrix4f projectionMatrix) {
        if(EFFECT_LEVEL != 0 || PREV_EFFECT_LEVEL != 0) {
            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);

            float effectLevel = Mth.lerp(partialTick, PREV_EFFECT_LEVEL, EFFECT_LEVEL);

            PoseStack matrices = new PoseStack();
            matrices.mulPose(positionMatrix);

            if (CYLINDER_BUFFER == null) {
                createCylinder();
            }

            float time = ((level.getGameTime() % 24000) + partialTick) / 24000.0F;

            RenderSystem.enableBlend();

            // default blend should already be set, but we set it again here just in case someone else were to (hypothetically) be messing with the render state incorrectly.
            RenderSystem.defaultBlendFunc();

            RenderSystem.disableDepthTest();
            RenderSystem.setShaderTexture(0, LEYFIELD_AZIMUTH_SHIMMER);
            for (int i = 1; i <= 5; i++) {
                matrices.pushPose();
                matrices.mulPose(Axis.YP.rotationDegrees((11 + 5 * i) * 360 * time * 2 + 0.4F * i));
                matrices.mulPose(Axis.XP.rotationDegrees((5 + 2 * i) * Mth.sin(Mth.TWO_PI * time * (5 - i) + 2.3F * i)));
                RenderSystem.setShaderColor(0.7F, 1, 0.8F, 0.3F * effectLevel);

                CYLINDER_BUFFER.bind();
                CYLINDER_BUFFER.drawWithShader(matrices.last().pose(), projectionMatrix, GameRenderer.getPositionTexColorShader());
                VertexBuffer.unbind();
                matrices.popPose();
            }
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
    }

    public static void update(@Nullable Level level, @Nullable Entity entity) {
        if(level == null || entity == null) {
            EFFECT_LEVEL = 0;
            PREV_EFFECT_LEVEL = 0;
        } else {
            Vec3 magFieldStrength = HyphaPiraceaLevelAttachment.getAttachment(level).getMagneticFieldAtPosition(entity.getEyePosition());
            double mag = magFieldStrength.length();

            double threshold = 1E-7;
            float target = 0;
            if(mag > threshold) {
                target = 1 - 1 / ((float)Math.log(mag / threshold));
            }

            PREV_EFFECT_LEVEL = EFFECT_LEVEL;

            EFFECT_LEVEL = Mth.lerp(0.02F, EFFECT_LEVEL, target);
            if(EFFECT_LEVEL < 0.0001) {
                EFFECT_LEVEL = 0;
            }
        }
    }
}
