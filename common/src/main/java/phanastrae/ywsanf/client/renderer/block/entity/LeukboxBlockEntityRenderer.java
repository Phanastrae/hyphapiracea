package phanastrae.ywsanf.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import phanastrae.ywsanf.block.entity.LeukboxBlockEntity;

public class LeukboxBlockEntityRenderer extends AbstractTextDisplayerBlockEntityRenderer<LeukboxBlockEntity> {

    public LeukboxBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(LeukboxBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        drawText(blockEntity.getTopText(), poseStack, bufferSource, new Vec3(0, 1.75, 0));

        Component bottomText = blockEntity.getBottomText();
        if(bottomText != null) {
            drawText(bottomText, poseStack, bufferSource, new Vec3(0, 1.5, 0));
        }
    }
}
