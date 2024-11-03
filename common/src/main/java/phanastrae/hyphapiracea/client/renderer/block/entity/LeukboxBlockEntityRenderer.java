package phanastrae.hyphapiracea.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import phanastrae.hyphapiracea.block.LeukboxBlock;
import phanastrae.hyphapiracea.block.entity.LeukboxBlockEntity;

public class LeukboxBlockEntityRenderer extends AbstractTextDisplayerBlockEntityRenderer<LeukboxBlockEntity> {

    public LeukboxBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(LeukboxBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        if(state.hasProperty(LeukboxBlock.FACING)) {
            Direction facingDirection = state.getValue(LeukboxBlock.FACING);
            poseStack.pushPose();
            poseStack.translate(0.5, 0.7, 0.5);

            Component topText = blockEntity.getTopText();
            drawTextOnSide(topText, poseStack, bufferSource, facingDirection, 0.75F);

            Component subtitleText = blockEntity.getBottomText();
            if(subtitleText != null) {
                poseStack.pushPose();
                poseStack.translate(0, -0.2, 0);
                drawTextOnSide(subtitleText, poseStack, bufferSource, facingDirection, 0.75F);
                poseStack.popPose();
            }

            Component structureText = blockEntity.getStructureText();
            if(structureText != null) {
                poseStack.pushPose();
                poseStack.translate(0, -0.35, 0);
                drawTextOnSide(structureText, poseStack, bufferSource, facingDirection, 0.75F);
                poseStack.popPose();
            }

            Component fieldStrengthText = blockEntity.getFieldStrengthText();
            if(fieldStrengthText != null) {
                poseStack.pushPose();
                poseStack.translate(0, -0.4, 0);
                drawTextOnSide(fieldStrengthText, poseStack, bufferSource, facingDirection, 0.75F);
                poseStack.popPose();
            }

            Component powerText = blockEntity.getPowerText();
            if(powerText != null) {
                poseStack.pushPose();
                poseStack.translate(0, -0.45, 0);
                drawTextOnSide(powerText, poseStack, bufferSource, facingDirection, 0.75F);
                poseStack.popPose();
            }

            poseStack.popPose();
        }
    }
}
