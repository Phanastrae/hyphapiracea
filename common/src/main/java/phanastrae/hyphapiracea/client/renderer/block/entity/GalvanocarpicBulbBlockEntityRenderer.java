package phanastrae.hyphapiracea.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import phanastrae.hyphapiracea.block.entity.GalvanocarpicBulbBlockEntity;

public class GalvanocarpicBulbBlockEntityRenderer extends AbstractTextDisplayerBlockEntityRenderer<GalvanocarpicBulbBlockEntity> {

    public GalvanocarpicBulbBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(GalvanocarpicBulbBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        // TODO change or remove
    }
}
