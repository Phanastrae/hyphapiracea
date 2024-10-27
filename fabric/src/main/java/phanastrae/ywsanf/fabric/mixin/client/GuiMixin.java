package phanastrae.ywsanf.fabric.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.ywsanf.client.YWSaNFClient;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderHotbarAndDecorations", at = @At(value = "RETURN"))
    private void ywsanf$renderGuiOverlayItemName(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        YWSaNFClient.renderGuiOverlayItemName(context);
    }
}
