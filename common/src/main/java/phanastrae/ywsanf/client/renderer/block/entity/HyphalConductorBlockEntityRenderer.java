package phanastrae.ywsanf.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.block.HyphalConductorBlock;
import phanastrae.ywsanf.block.entity.HyphalConductorBlockEntity;
import phanastrae.ywsanf.block.state.ConductorStateProperty;
import phanastrae.ywsanf.client.renderer.entity.model.YWSaNFEntityModelLayers;

public class HyphalConductorBlockEntityRenderer implements BlockEntityRenderer<HyphalConductorBlockEntity> {
    private static final ResourceLocation KNOT_LOCATION = YWSaNF.id("textures/entity/hyphaline_coil.png");
    private final LeashKnotModel<LeashFenceKnotEntity> model;
    private final LeashKnotModel<LeashFenceKnotEntity> smallModel;

    public HyphalConductorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new LeashKnotModel<>(context.bakeLayer(YWSaNFEntityModelLayers.HYPHALINE_COIL));
        this.smallModel = new LeashKnotModel<>(context.bakeLayer(YWSaNFEntityModelLayers.HYPHALINE_COIL_SMALL));
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public void render(HyphalConductorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();

        // render link
        if(level != null && blockEntity.hasItem()) {
            Vec3 linkPos = getLinkPosition(blockEntity, partialTick);
            if(linkPos != null) {
                poseStack.pushPose();
                poseStack.translate(0.5, 0.5, 0.5);

                Vec3 thisPos = blockEntity.getBlockPos().getCenter();
                BlockPos targetBlockPos = BlockPos.containing(linkPos);

                int startBlockLight = LightTexture.block(packedLight);
                int startSkyLight = LightTexture.sky(packedLight);

                int endBlockLight = level.getBrightness(LightLayer.BLOCK, targetBlockPos);
                int endSkyLight = level.getBrightness(LightLayer.SKY, targetBlockPos);

                this.renderWire(poseStack, bufferSource, thisPos, linkPos, startBlockLight, endBlockLight, startSkyLight, endSkyLight);

                poseStack.popPose();
            }
        }

        BlockState state = blockEntity.getBlockState();
        if(state.hasProperty(HyphalConductorBlock.CONDUCTOR_STATE) && state.hasProperty(HyphalConductorBlock.FACING)) {
            ConductorStateProperty.ConductorState conductorState = state.getValue(HyphalConductorBlock.CONDUCTOR_STATE);
            Direction direction = state.getValue(HyphalConductorBlock.FACING);

            if(conductorState != ConductorStateProperty.ConductorState.EMPTY) {
                Model useModel = conductorState == ConductorStateProperty.ConductorState.HOLDING_WIRE ? this.model : this.smallModel;

                poseStack.pushPose();
                poseStack.translate(0.5, 0.5, 0.5);

                if (direction.getAxis().isHorizontal()) {
                    poseStack.mulPose(direction.getRotation());
                    poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-67.5F)));
                }

                poseStack.scale(-1.0F, -1.0F, 1.0F);

                VertexConsumer vertexconsumer = bufferSource.getBuffer(useModel.renderType(KNOT_LOCATION));
                useModel.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);

                poseStack.popPose();
            }
        }
    }

    @Nullable
    public Vec3 getLinkPosition(HyphalConductorBlockEntity blockEntity, float partialTick) {
        Entity linkedEntity = blockEntity.getLinkedEntity();
        if(linkedEntity != null) {
            return linkedEntity.getRopeHoldPosition(partialTick);
        } else {
            BlockPos pos = blockEntity.getLinkedBlockPos();
            if(pos != null) {
                return pos.getCenter();
            }
        }

        return null;
    }

    private void renderWire(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 thisPos, Vec3 targetPos, int startBlockLight, int endBlockLight, int startSkyLight, int endSkyLight) {
        poseStack.pushPose();
        float relX = (float)(targetPos.x - thisPos.x);
        float relY = (float)(targetPos.y - thisPos.y);
        float relZ = (float)(targetPos.z - thisPos.z);

        float scale = 0.1F;

        float normaliseAndScale = Mth.invSqrt(relX * relX + relZ * relZ) * scale / 2.0F;
        float dx = relZ * normaliseAndScale;
        float dz = relX * normaliseAndScale;

        boolean applyVerticalFix = Math.abs(relX) < 0.001 && Math.abs(relZ) < 0.001;

        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.leash());
        Matrix4f matrix4f = poseStack.last().pose();

        if(applyVerticalFix) {
            dx = scale / 2.0F;
            dz = scale / 2.0F;
        }
        for (int i = 0; i <= 24; i++) {
            addVertexPair(vertexconsumer, matrix4f, relX, relY, relZ, startBlockLight, endBlockLight, startSkyLight, endSkyLight, scale, scale, dx, dz, i, false);
        }

        if(applyVerticalFix) {
            dx = scale / 2.0F;
            dz = -scale / 2.0F;
        }
        for (int i = 24; i >= 0; i--) {
            addVertexPair(vertexconsumer, matrix4f, relX, relY, relZ, startBlockLight, endBlockLight, startSkyLight, endSkyLight, scale, 0.0F, dx, dz, i, true);
        }
        poseStack.popPose();
    }

    private static void addVertexPair(
            VertexConsumer buffer,
            Matrix4f pose,
            float startX,
            float startY,
            float startZ,
            int entityBlockLight,
            int holderBlockLight,
            int entitySkyLight,
            int holderSkyLight,
            float yOffset,
            float dy,
            float dx,
            float dz,
            int index,
            boolean reverse
    ) {
        float lerpFactor = (float)index / 24.0F;

        int lerpedBlockLight = (int)Mth.lerp(lerpFactor, (float)entityBlockLight, (float)holderBlockLight);
        int lerpedSkyLight = (int)Mth.lerp(lerpFactor, (float)entitySkyLight, (float)holderSkyLight);

        int light = LightTexture.pack(lerpedBlockLight, lerpedSkyLight);

        float brightness = index % 2 == (reverse ? 1 : 0) ? 0.7F : 1.0F;

        float r = 0.7F * brightness;
        float g = 0.7F * brightness;
        float b = 0.5F * brightness;

        float lerpedX = startX * lerpFactor;
        float lerpedY = startY > 0.0F ? startY * lerpFactor * lerpFactor : startY - startY * (1.0F - lerpFactor) * (1.0F - lerpFactor);
        float lerpedZ = startZ * lerpFactor;

        buffer.addVertex(pose, lerpedX - dx, lerpedY - 0.5F * yOffset + dy, lerpedZ + dz).setColor(r, g, b, 1.0F).setLight(light);
        buffer.addVertex(pose, lerpedX + dx, lerpedY + 0.5F * yOffset - dy, lerpedZ - dz).setColor(r, g, b, 1.0F).setLight(light);
    }

    public static LayerDefinition createTallCoilBodyLayer() {
        return createCoilBodyLayer(10);
    }

    public static LayerDefinition createSmallCoilBodyLayer() {
        return createCoilBodyLayer(4);
    }

    public static LayerDefinition createCoilBodyLayer(int size) {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("knot",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0F, -0.5F * size, -2.0F, 4.0F, size, 4.0F), PartPose.ZERO);
        return LayerDefinition.create(meshdefinition, 32, 32);
    }
}
