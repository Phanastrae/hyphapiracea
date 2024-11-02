package phanastrae.hyphapiracea.mixin;

import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.hyphapiracea.structure.IntermediateGenLevel;

@Mixin(StructurePiece.class)
public class StructurePieceMixin {

    @Inject(method = "placeBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/WorldGenLevel;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z", shift = At.Shift.AFTER), cancellable = true)
    private void hyphapiracea$skipBlockUpdates(WorldGenLevel level, BlockState blockstate, int x, int y, int z, BoundingBox boundingbox, CallbackInfo ci) {
        // when generating a structure for gradual placement, exit early to avoid mark blocks/fluids for updates
        if(level instanceof IntermediateGenLevel igl) {
            if(igl.skipUpdatesInStructurePiece()) {
                ci.cancel();
            }
        }
    }
}
