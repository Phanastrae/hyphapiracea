package phanastrae.ywsanf.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.client.renderer.block.entity.YWSaNFBlockEntityRenderers;
import phanastrae.ywsanf.item.MagnetometerItem;
import phanastrae.ywsanf.item.YWSaNFItems;
import phanastrae.ywsanf.mixin.client.ItemPropertiesAccessor;
import phanastrae.ywsanf.world.YWSaNFLevelAttachment;

public class YWSaNFClient {

    public static void init() {
        // register block entity renderers
        YWSaNFBlockEntityRenderers.init();

        // register item properties
        YWSaNFClient.registerItemProperties();
    }

    public static void renderGuiOverlayItemName(GuiGraphics guiGraphics) {
        Minecraft client = Minecraft.getInstance();

        MultiPlayerGameMode gameMode = client.gameMode;
        if(gameMode == null || gameMode.getPlayerMode() == GameType.SPECTATOR) return;

        Entity cameraEntity = client.getCameraEntity();
        if(!(cameraEntity instanceof Player player)) return;

        ItemStack stack = player.getMainHandItem();
        if(stack.isEmpty()) return;

        if(stack.getItem() instanceof MagnetometerItem) {
            Font font = client.font;
            Vec3 magneticField = YWSaNFLevelAttachment.getAttachment(player.level()).getMagneticFieldAtPosition(player.position());

            double fieldStrengthTesla = magneticField.length();
            double fieldStrengthMicroTesla = fieldStrengthTesla * 1E6;

            String s = String.format("%1$,.6f", fieldStrengthMicroTesla);
            Component component = Component.translatable("gui.ywsanf.magnetometer.field_strength_microtesla", s).withStyle(ChatFormatting.GREEN);

            int width = font.width(component);
            int x = (guiGraphics.guiWidth() - width) / 2;
            int y = guiGraphics.guiHeight() - 59;
            if (!gameMode.canHurtPlayer()) {
                y += 14;
            }

            y -= 14;

            guiGraphics.drawStringWithBackdrop(font, component, x, y, width, FastColor.ARGB32.color(255, -1));
        }
    }

    public static void registerItemProperties() {
        ItemPropertiesAccessor.invokeRegister(
                YWSaNFItems.MAGNETOMETER,
                YWSaNF.id("off"),
                (stack, level, entity, i) -> entity instanceof Player player && player.getCooldowns().isOnCooldown(YWSaNFItems.MAGNETOMETER) ? 1.0F : 0.0F
        );
    }
}
