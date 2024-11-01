package phanastrae.ywsanf.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import phanastrae.ywsanf.block.entity.StormsapCellBlockEntity;

public class StormsapCellBlockEntityRenderer extends AbstractTextDisplayerBlockEntityRenderer<StormsapCellBlockEntity> {

    public StormsapCellBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(StormsapCellBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Component energy3 = Component.translatable("ywsanf.displays.joule", blockEntity.getStoredEnergy()).withStyle(ChatFormatting.GREEN);
        drawTexts(poseStack, bufferSource, energy3);
    }
}
