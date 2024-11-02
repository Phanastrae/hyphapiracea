package phanastrae.hyphapiracea.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import phanastrae.hyphapiracea.block.entity.VoltmeterBlockEntity;
import phanastrae.hyphapiracea.block.state.HyphaPiraceaBlockProperties;

public class VoltmeterBlockEntityRenderer extends AbstractTextDisplayerBlockEntityRenderer<VoltmeterBlockEntity> {

    public VoltmeterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(VoltmeterBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if(blockEntity.getLevel() != null) {
            long levelTime = blockEntity.getLevel().getGameTime();
            double scale = getScaleForTime(levelTime, blockEntity.lastHighlightTime, partialTick, blockEntity.getBlockState());

            if(scale > 0.0) {
                double voltage = blockEntity.getVoltage();
                String formatted = String.format("%1$,.2f", voltage);
                Component voltageComponent = Component.translatable("hyphapiracea.displays.volt", formatted).withStyle(ChatFormatting.LIGHT_PURPLE);
                drawTextOnAllSides(voltageComponent, poseStack, bufferSource, blockEntity.getLevel(), blockEntity.getBlockPos(), scale);
            }
        }
    }

    public static double getScaleForTime(long levelTime, long lastHighlightTime, float partialTick, BlockState thisState) {
        if(thisState.hasProperty(HyphaPiraceaBlockProperties.ALWAYS_SHOW_INFO)) {
            if(thisState.getValue(HyphaPiraceaBlockProperties.ALWAYS_SHOW_INFO)) {
                return 1.0;
            }
        }

        if(lastHighlightTime == -1) {
            return 0.0;
        } else if(levelTime <= lastHighlightTime) {
            return 1.0;
        } else {
            long dt = levelTime - lastHighlightTime;
            if(dt > 200) {
                return 0.0;
            } else {
                float dtf = dt + partialTick - 1;
                float t = Math.clamp(1 - dtf / 160, 0, 1);
                return 1 - (1 - t) * (1 - t);
            }
        }
    }
}
