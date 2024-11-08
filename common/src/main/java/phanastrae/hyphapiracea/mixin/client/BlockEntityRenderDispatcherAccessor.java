package phanastrae.hyphapiracea.mixin.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockEntityRenderDispatcher.class)
public interface BlockEntityRenderDispatcherAccessor {

    @Invoker
    static void invokeTryRender(BlockEntity blockEntity, Runnable renderer) {
        throw new AssertionError();
    }
}
