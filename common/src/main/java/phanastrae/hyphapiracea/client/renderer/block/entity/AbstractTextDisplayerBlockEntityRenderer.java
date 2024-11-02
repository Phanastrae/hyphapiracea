package phanastrae.hyphapiracea.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

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

    public void drawTexts(PoseStack poseStack, MultiBufferSource bufferSource, Component... texts) {
        this.drawTexts(poseStack, bufferSource, new Vec3(0, 1.25, 0), new Vec3(0, 0.25, 0), texts);
    }

    public void drawTexts(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 offset, Vec3 componentOffset, Component... texts) {
        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);
        for(int i = 0; i < texts.length; i++) {
            Component component = texts[i];
            drawText(component, poseStack, bufferSource, componentOffset.scale(texts.length - i - 1));
        }
        poseStack.popPose();
    }

    public void drawTextOnAllSides(Component text, PoseStack poseStack, MultiBufferSource bufferSource, Level level, BlockPos pos, double scale) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        for(Direction direction : Direction.values()) {
            if(direction.getAxis().isHorizontal()) {
                if(level != null) {
                    BlockPos adjacentPos = pos.offset(direction.getNormal());
                    BlockState adjacentBlockState = level.getBlockState(adjacentPos);
                    if(!adjacentBlockState.isSolidRender(level, adjacentPos)) {
                        drawTextOnSide(text, poseStack, bufferSource, direction, (float)scale);
                    }
                }
            }
        }
        poseStack.popPose();
    }

    public void drawTextOnSide(Component text, PoseStack poseStack, MultiBufferSource bufferSource, Direction side, float scale) {
        poseStack.pushPose();

        poseStack.mulPose(new Quaternionf().rotateY(-(float)Math.toRadians(side.toYRot())));
        poseStack.translate(0, 0, 0.5625);
        poseStack.scale(scale, scale, scale);

        drawTextBasic(text, poseStack, bufferSource, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
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

    public void drawTextBasic(Component text, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        double width = font.width(text);
        double size = Math.max(width, 40);
        float f = (float)(1 / size) * 0.875F;
        poseStack.scale(f, -f, f);

        float x = (float)(-width / 2);
        float y = (float)(-font.lineHeight) / 2;
        this.font.drawInBatch(
                text, x, y, -1, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, 0, packedLight
        );
        poseStack.popPose();
    }
}
