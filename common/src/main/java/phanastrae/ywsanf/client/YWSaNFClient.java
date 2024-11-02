package phanastrae.ywsanf.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.client.renderer.block.entity.YWSaNFBlockEntityRenderers;
import phanastrae.ywsanf.item.MagnetometerItem;
import phanastrae.ywsanf.item.YWSaNFItems;
import phanastrae.ywsanf.mixin.client.ItemPropertiesAccessor;
import phanastrae.ywsanf.particle.YWSaNFParticleTypes;
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

    public static void startClientTick() {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        Player player = client.player;
        if(level != null) {
            if(player != null) {
                RandomSource randomSource = player.getRandom();

                for(int i = 0; i < 3; i++) {
                    if (randomSource.nextFloat() > 0.5) {
                        double dx = (randomSource.nextFloat() * 2.0 - 1.0);
                        double dy = (randomSource.nextFloat() * 2.0 - 1.0);
                        double dz = (randomSource.nextFloat() * 2.0 - 1.0);
                        dx *= dx * Mth.sign(dx);
                        dy *= dy * Mth.sign(dy);
                        dz *= dz * Mth.sign(dz);
                        Vec3 dv = new Vec3(dx, dy, dz);
                        dv = dv.normalize();
                        dv = dv.scale(4.0 + randomSource.nextFloat() * randomSource.nextFloat() * 20);

                        Vec3 pos = player.position().add(dv);

                        BlockPos bp = BlockPos.containing(pos);
                        BlockState s = level.getBlockState(bp);
                        if(s.isAir() || s.canBeReplaced()) {
                            Vec3 magField = YWSaNFLevelAttachment.getAttachment(level).getMagneticFieldAtPosition(pos);
                            double magFieldStrength = magField.length();
                            if (magFieldStrength > 2E-7) {
                                double p = Math.min(Math.log(magFieldStrength * 5E6) * 30 * randomSource.nextFloat() * randomSource.nextFloat(), 9);

                                if (p > 1) {
                                    spawnFieldLine(level, pos, Mth.floor(p), 0.18);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void spawnFieldLine(Level level, Vec3 startPos, int length, double distance) {
        MagnetometerItem.spawnFieldLine(level, startPos, length, distance, vec3 -> {
                    RandomSource randomSource = level.random;
                    level.addParticle(YWSaNFParticleTypes.FAIRY_FOG, true,
                            vec3.x + (randomSource.nextFloat() * 2.0 - 1.0) * 0.3,
                            vec3.y + (randomSource.nextFloat() * 2.0 - 1.0) * 0.3,
                            vec3.z + (randomSource.nextFloat() * 2.0 - 1.0) * 0.3,
                            (randomSource.nextFloat() * 2.0 - 1.0) * 0.05,
                            (randomSource.nextFloat() * 2.0 - 1.0) * 0.05,
                            (randomSource.nextFloat() * 2.0 - 1.0) * 0.05
                    );
                }
        );
    }
}
