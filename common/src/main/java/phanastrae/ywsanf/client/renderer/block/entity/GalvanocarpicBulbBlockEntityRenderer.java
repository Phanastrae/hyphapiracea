package phanastrae.ywsanf.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import phanastrae.ywsanf.block.entity.GalvanocarpicBulbBlockEntity;

public class GalvanocarpicBulbBlockEntityRenderer extends AbstractTextDisplayerBlockEntityRenderer<GalvanocarpicBulbBlockEntity> {

    public GalvanocarpicBulbBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(GalvanocarpicBulbBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Component charge = Component.translatable("ywsanf.displays.millicoulomb", blockEntity.getChargeSac().getChargeMilliCoulombs());
        drawText(charge, poseStack, bufferSource, new Vec3(0, 1.25, 0));
    }
}
