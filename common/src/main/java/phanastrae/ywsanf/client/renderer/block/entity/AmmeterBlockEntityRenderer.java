package phanastrae.ywsanf.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import phanastrae.ywsanf.block.entity.AmmeterBlockEntity;

public class AmmeterBlockEntityRenderer extends AbstractTextDisplayerBlockEntityRenderer<AmmeterBlockEntity> {

    public AmmeterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(AmmeterBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Component amperage = Component.translatable("ywsanf.displays.amp", blockEntity.getCurrent()).withStyle(ChatFormatting.YELLOW);
        drawTexts(poseStack, bufferSource, amperage);
    }
}
