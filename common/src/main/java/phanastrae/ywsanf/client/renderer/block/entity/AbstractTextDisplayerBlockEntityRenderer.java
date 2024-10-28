package phanastrae.ywsanf.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public abstract class AbstractTextDisplayerBlockEntityRenderer <T extends BlockEntity> implements BlockEntityRenderer<T> {

    private final Font font;
    private final EntityRenderDispatcher entityRenderDispatcher;

    public AbstractTextDisplayerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
        this.entityRenderDispatcher = context.getEntityRenderer();
    }

    @Override
    public void render(T blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
    }

    public void drawText(Component text, PoseStack poseStack, MultiBufferSource bufferSource, Vec3 offset) {
        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.translate(offset.x, offset.y, offset.z);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.025F, -0.025F, 0.025F);

        drawText(text, poseStack.last().pose(), bufferSource, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }

    public void drawText(Component text, Matrix4f matrix4f, MultiBufferSource bufferSource, int packedLight) {
        float backgroundOpacity = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int backgroundColor = (int)(backgroundOpacity * 255.0F) << 24;

        float x = (float)(-font.width(text) / 2);
        float y = 0;
        this.font.drawInBatch(
                text, x, y, 553648127, false, matrix4f, bufferSource, Font.DisplayMode.SEE_THROUGH, backgroundColor, packedLight
        );
        this.font.drawInBatch(
                text, x, y, -1, false, matrix4f, bufferSource, Font.DisplayMode.NORMAL, 0, packedLight
        );
    }
}
