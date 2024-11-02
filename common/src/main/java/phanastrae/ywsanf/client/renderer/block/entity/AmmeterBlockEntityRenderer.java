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
        if(blockEntity.getLevel() != null) {
            long levelTime = blockEntity.getLevel().getGameTime();
            double scale = VoltmeterBlockEntityRenderer.getScaleForTime(levelTime, blockEntity.lastHighlightTime, partialTick, blockEntity.getBlockState());

            if(scale > 0.0) {
                double current = blockEntity.getCurrent();
                String formatted = String.format("%1$,.2f", current);
                Component currentComponent = Component.translatable("ywsanf.displays.amp", formatted).withStyle(ChatFormatting.YELLOW);
                drawTextOnAllSides(currentComponent, poseStack, bufferSource, blockEntity.getLevel(), blockEntity.getBlockPos(), scale);
            }
        }
    }
}
