package phanastrae.ywsanf.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import phanastrae.ywsanf.block.entity.VoltmeterBlockEntity;

public class VoltmeterBlockEntityRenderer extends AbstractTextDisplayerBlockEntityRenderer<VoltmeterBlockEntity> {

    public VoltmeterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(VoltmeterBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        double pd = blockEntity.getVoltage();
        String formatted = String.format("%1$,.8f", pd);
        Component voltage = Component.translatable("ywsanf.displays.volt", formatted).withStyle(ChatFormatting.GOLD);
        drawTexts(poseStack, bufferSource, voltage);
    }
}
