package phanastrae.hyphapiracea.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import phanastrae.hyphapiracea.block.entity.StormsapCellBlockEntity;

public class StormsapCellBlockEntityRenderer extends AbstractTextDisplayerBlockEntityRenderer<StormsapCellBlockEntity> {

    public StormsapCellBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(StormsapCellBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if(blockEntity.getLevel() != null) {
            long levelTime = blockEntity.getLevel().getGameTime();
            double scale = VoltmeterBlockEntityRenderer.getScaleForTime(levelTime, blockEntity.lastHighlightTime, partialTick, blockEntity.getBlockState());

            if(scale > 0.0) {
                if (blockEntity.isInfinite()) {
                    Component energyComponent = Component.translatable("hyphapiracea.displays.joule", Component.translatable("hyphapiracea.displays.infinity")).withStyle(ChatFormatting.AQUA);
                    drawTextOnAllSides(energyComponent, poseStack, bufferSource, blockEntity.getLevel(), blockEntity.getBlockPos(), scale);
                } else {
                    double energy = blockEntity.getPositiveStoredEnergy();
                    String formatted = String.format("%1$,.0f", energy);
                    Component energyComponent = Component.translatable("hyphapiracea.displays.joule", formatted).withStyle(ChatFormatting.AQUA);
                    drawTextOnAllSides(energyComponent, poseStack, bufferSource, blockEntity.getLevel(), blockEntity.getBlockPos(), scale);
                }
            }
        }
    }
}
