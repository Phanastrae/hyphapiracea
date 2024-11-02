package phanastrae.hyphapiracea.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
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
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import phanastrae.hyphapiracea.block.HyphalConductorBlock;
import phanastrae.hyphapiracea.block.entity.HyphalConductorBlockEntity;
import phanastrae.hyphapiracea.block.state.ConductorStateProperty;
import phanastrae.hyphapiracea.client.renderer.entity.model.HyphaPiraceaEntityModelLayers;
import phanastrae.hyphapiracea.component.type.WireLineComponent;
import phanastrae.hyphapiracea.entity.HyphaPiraceaEntityAttachment;

public class HyphalConductorBlockEntityRenderer implements BlockEntityRenderer<HyphalConductorBlockEntity> {
    private final LeashKnotModel<LeashFenceKnotEntity> model;
    private final LeashKnotModel<LeashFenceKnotEntity> smallModel;

    public HyphalConductorBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new LeashKnotModel<>(context.bakeLayer(HyphaPiraceaEntityModelLayers.HYPHALINE_COIL));
        this.smallModel = new LeashKnotModel<>(context.bakeLayer(HyphaPiraceaEntityModelLayers.HYPHALINE_COIL_SMALL));
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public void render(HyphalConductorBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();

        boolean isSource = true;
        WireLineComponent wireLineComponent = blockEntity.getWireLineComponent();
        if(wireLineComponent == null && level != null) {
            BlockPos linkedPos = blockEntity.getLinkedBlockPos();
            if(linkedPos != null && level.getBlockEntity(linkedPos) instanceof HyphalConductorBlockEntity linkedHCBE) {
                wireLineComponent = linkedHCBE.getWireLineComponent();
                isSource = false;
            }
        }
        if(wireLineComponent != null) {

            // render link
            if (level != null && isSource) {
                Vec3 linkPos = getLinkPosition(blockEntity, partialTick);
                if (linkPos != null) {
                    poseStack.pushPose();
                    poseStack.translate(0.5, 0.5, 0.5);

                    Vec3 thisPos = blockEntity.getBlockPos().getCenter();
                    BlockPos targetBlockPos = BlockPos.containing(linkPos);

                    int startBlockLight = LightTexture.block(packedLight);
                    int startSkyLight = LightTexture.sky(packedLight);

                    int endBlockLight = level.getBrightness(LightLayer.BLOCK, targetBlockPos);
                    int endSkyLight = level.getBrightness(LightLayer.SKY, targetBlockPos);

                    float reachRatio = 0;
                    if (blockEntity.getLinkedEntity() != null) {
                        float distance = (float) thisPos.distanceTo(linkPos);
                        float maxReach = blockEntity.getMaxWireRange();
                        reachRatio = distance / maxReach;
                    }

                    this.renderWire(poseStack, bufferSource, wireLineComponent, thisPos, linkPos, startBlockLight, endBlockLight, startSkyLight, endSkyLight, reachRatio);

                    poseStack.popPose();
                }
            }

            // draw coil
            BlockState state = blockEntity.getBlockState();
            if(state.hasProperty(HyphalConductorBlock.FACING)) {
                Direction direction = state.getValue(HyphalConductorBlock.FACING);

                Model useModel = isSource ? this.model : this.smallModel;

                poseStack.pushPose();
                poseStack.translate(0.5, 0.5, 0.5);

                if (direction.getAxis().isHorizontal()) {
                    poseStack.mulPose(direction.getRotation());
                    poseStack.mulPose(new Quaternionf().rotateX((float) Math.toRadians(-67.5F)));
                }

                poseStack.scale(-1.0F, -1.0F, 1.0F);

                VertexConsumer vertexconsumer = bufferSource.getBuffer(useModel.renderType(wireLineComponent.getTextureFull()));
                useModel.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);

                poseStack.popPose();
            }
        }
    }

    @Nullable
    public Vec3 getLinkPosition(HyphalConductorBlockEntity blockEntity, float partialTick) {
        Entity linkedEntity = blockEntity.getLinkedEntity();
        if(linkedEntity != null) {
            Minecraft client = Minecraft.getInstance();
            if(client.cameraEntity == linkedEntity && client.options.getCameraType().isFirstPerson() && linkedEntity instanceof LivingEntity livingEntity && HyphaPiraceaEntityAttachment.getAttachment(linkedEntity).getFirstLink() == blockEntity) {
                AttributeInstance reachAttribute = livingEntity.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
                double reach = reachAttribute == null ? 4.5 : reachAttribute.getValue();

                Vec3 eyePos = linkedEntity.getEyePosition(partialTick);
                Vec3 viewVec = linkedEntity.getViewVector(partialTick);

                HitResult hitResult = linkedEntity.pick(reach, partialTick, false);
                Vec3 pos;
                if(hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockHitResult blockHitResult = ((BlockHitResult)hitResult);

                    pos = hitResult.getLocation();
                    Vec3i normal = blockHitResult.getDirection().getNormal();
                    pos = pos.add(normal.getX() * 0.05, normal.getY() * 0.05, normal.getZ() * 0.05);

                    Level level = blockEntity.getLevel();
                    if(level != null) {
                        BlockState state = level.getBlockState(blockHitResult.getBlockPos());
                        if(state.getBlock() instanceof HyphalConductorBlock && state.hasProperty(HyphalConductorBlock.CONDUCTOR_STATE) && state.getValue(HyphalConductorBlock.CONDUCTOR_STATE) != ConductorStateProperty.ConductorState.HOLDING_WIRE) {
                            if(level.getBlockEntity(blockHitResult.getBlockPos()) instanceof HyphalConductorBlockEntity targetHCBE) {
                                if(targetHCBE.getLinkedBlockPos() == null) {
                                    Vec3 target = blockHitResult.getBlockPos().getCenter();
                                    if (blockEntity.canLinkTo(target)) {
                                        return target;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    pos = eyePos.add(viewVec.scale(reach));
                }

                Vec3 sourcePos = blockEntity.getBlockPos().getCenter();
                double distance = sourcePos.distanceTo(pos);
                double maxDistance = blockEntity.getMaxWireRange();
                if(distance > maxDistance) {
                    Vec3 offset = pos.subtract(sourcePos);
                    double overDistance = distance - maxDistance;

                    Vec3 adjust = viewVec.scale(-overDistance * distance / viewVec.dot(offset));
                    pos = pos.add(adjust);
                }

                return pos;
            } else {
                return linkedEntity.getRopeHoldPosition(partialTick);
            }
        } else {
            BlockPos pos = blockEntity.getLinkedBlockPos();
            if(pos != null) {
                return pos.getCenter();
            }
        }

        return null;
    }

    private void renderWire(PoseStack poseStack, MultiBufferSource bufferSource, WireLineComponent wireLineComponent, Vec3 thisPos, Vec3 targetPos, int startBlockLight, int endBlockLight, int startSkyLight, int endSkyLight, float reachRatio) {
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
            addVertexPair(vertexconsumer, matrix4f, wireLineComponent, relX, relY, relZ, startBlockLight, endBlockLight, startSkyLight, endSkyLight, scale, scale, dx, dz, i, false, reachRatio);
        }

        if(applyVerticalFix) {
            dx = scale / 2.0F;
            dz = -scale / 2.0F;
        }
        for (int i = 24; i >= 0; i--) {
            addVertexPair(vertexconsumer, matrix4f, wireLineComponent, relX, relY, relZ, startBlockLight, endBlockLight, startSkyLight, endSkyLight, scale, 0.0F, dx, dz, i, true, reachRatio);
        }
        poseStack.popPose();
    }

    private static void addVertexPair(VertexConsumer buffer, Matrix4f pose, WireLineComponent wireLineComponent, float startX, float startY, float startZ, int entityBlockLight, int holderBlockLight, int entitySkyLight, int holderSkyLight, float yOffset, float dy, float dx, float dz, int index, boolean reverse, float reachRatio) {
        float lerpFactor = (float)index / 24.0F;

        int lerpedBlockLight = (int)Mth.lerp(lerpFactor, (float)entityBlockLight, (float)holderBlockLight);
        int lerpedSkyLight = (int)Mth.lerp(lerpFactor, (float)entitySkyLight, (float)holderSkyLight);
        int light = LightTexture.pack(lerpedBlockLight, lerpedSkyLight);

        float lerpedReachRatio = reachRatio * lerpFactor;
        float adjustedReachRatio = Math.clamp(lerpedReachRatio * 2 - 1, 0, 1);
        float dimFactor = 1.0F - 0.7F * adjustedReachRatio * adjustedReachRatio;

        boolean useDark = index % 2 == (reverse ? 1 : 0);
        Vector3f lightColor = wireLineComponent.lightColor();
        Vector3f darkColor = wireLineComponent.darkColor();

        float r = (useDark ? darkColor.x : lightColor.x) * dimFactor + 0.7F * adjustedReachRatio * adjustedReachRatio;
        float g = (useDark ? darkColor.y : lightColor.y) * dimFactor;
        float b = (useDark ? darkColor.z : lightColor.z) * dimFactor;

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
