package phanastrae.ywsanf.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import phanastrae.ywsanf.block.entity.VoltmeterBlockEntity;

public class VoltmeterBlockEntityRenderer extends AbstractTextDisplayerBlockEntityRenderer<VoltmeterBlockEntity> {

    public VoltmeterBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(VoltmeterBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Component charge = Component.translatable("ywsanf.displays.millicoulomb", blockEntity.getChargeSac().getChargeMilliCoulombs());
        drawText(charge, poseStack, bufferSource, new Vec3(0, 1.75, 0));

        Component charge2 = Component.translatable("ywsanf.displays.millicoulomb", blockEntity.getSecondaryChargeSac().getChargeMilliCoulombs());
        drawText(charge2, poseStack, bufferSource, new Vec3(0, 1.5, 0));

        double pd = blockEntity.getSecondaryChargeSac().getVoltage() - blockEntity.getChargeSac().getVoltage();
        String formatted = String.format("%1$,.2f", pd);
        Component voltage = Component.translatable("ywsanf.displays.volt", formatted);
        drawText(voltage, poseStack, bufferSource, new Vec3(0, 1.25, 0));
    }
}
