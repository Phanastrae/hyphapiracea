package phanastrae.hyphapiracea.fabric.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.hyphapiracea.client.HyphaPiraceaClient;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderHotbarAndDecorations", at = @At(value = "RETURN"))
    private void hyphapiracea$renderGuiOverlayItemName(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        HyphaPiraceaClient.renderGuiOverlayItemName(context);
    }
}
