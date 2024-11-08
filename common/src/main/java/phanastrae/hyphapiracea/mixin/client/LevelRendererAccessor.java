package phanastrae.hyphapiracea.mixin.client;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {

    @Accessor
    BlockEntityRenderDispatcher getBlockEntityRenderDispatcher();

    @Accessor
    RenderBuffers getRenderBuffers();

    @Accessor
    Frustum getCullingFrustum();
}
