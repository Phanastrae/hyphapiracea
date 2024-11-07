package phanastrae.hyphapiracea.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColor;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.block.HyphaPiraceaBlocks;
import phanastrae.hyphapiracea.block.StormsapCellBlock;
import phanastrae.hyphapiracea.block.entity.ClientHighlightReactingBlockEntity;
import phanastrae.hyphapiracea.client.renderer.LeyfieldEnvironmentEffects;
import phanastrae.hyphapiracea.client.renderer.block.HyphaPiraceaBlockRenderLayers;
import phanastrae.hyphapiracea.client.renderer.block.entity.HyphaPiraceaBlockEntityRenderers;
import phanastrae.hyphapiracea.item.HyphaPiraceaItems;
import phanastrae.hyphapiracea.item.MagnetometerItem;
import phanastrae.hyphapiracea.mixin.client.ItemPropertiesAccessor;
import phanastrae.hyphapiracea.particle.HyphaPiraceaParticleTypes;
import phanastrae.hyphapiracea.world.HyphaPiraceaLevelAttachment;

public class HyphaPiraceaClient {

    public static void init() {
        // register block layers
        HyphaPiraceaBlockRenderLayers.init();

        // register block entity renderers
        HyphaPiraceaBlockEntityRenderers.init();

        // register item properties
        HyphaPiraceaClient.registerItemProperties();
    }

    public static void renderGuiOverlayItemName(GuiGraphics guiGraphics) {
        Minecraft client = Minecraft.getInstance();

        MultiPlayerGameMode gameMode = client.gameMode;
        if(gameMode == null || gameMode.getPlayerMode() == GameType.SPECTATOR) return;

        Entity cameraEntity = client.getCameraEntity();
        if(!(cameraEntity instanceof Player player)) return;

        ItemStack stack = player.getMainHandItem();
        ItemStack stack2 = player.getOffhandItem();

        if((!stack.isEmpty() && stack.getItem() instanceof MagnetometerItem) || (!stack2.isEmpty() && stack2.getItem() instanceof MagnetometerItem)) {
            Font font = client.font;
            HyphaPiraceaLevelAttachment hpla = HyphaPiraceaLevelAttachment.getAttachment(player.level());
            Vec3 samplePos = player.getEyePosition();
            Vec3 magneticField = hpla.getMagneticFieldAtPosition(samplePos);
            boolean warded = hpla.isPositionWarded(samplePos);

            double fieldStrengthTesla = magneticField.length();
            double fieldStrengthMicroTesla = fieldStrengthTesla * 1E6;

            String s = String.format("%1$,.6f", fieldStrengthMicroTesla);
            Component component = Component.translatable("gui.hyphapiracea.magnetometer.field_strength_microtesla", s).withStyle(ChatFormatting.GREEN);

            int width = font.width(component);
            int x = (guiGraphics.guiWidth() - width) / 2;
            int y = guiGraphics.guiHeight() - 59;
            if (!gameMode.canHurtPlayer()) {
                y += 14;
            }

            y -= 14;

            guiGraphics.drawStringWithBackdrop(font, component, x, y, width, FastColor.ARGB32.color(255, -1));

            if(warded) {
                y -= 14;

                Component component2 = Component.translatable("gui.hyphapiracea.magnetometer.warded").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
                int width2 = font.width(component2);
                x = (guiGraphics.guiWidth() - width2) / 2;
                guiGraphics.drawStringWithBackdrop(font, component2, x, y, width, FastColor.ARGB32.color(255, -1));
            }
        }
    }

    public static void registerItemProperties() {
        ItemPropertiesAccessor.invokeRegister(
                HyphaPiraceaItems.LEYFIELD_MAGNETOMETER,
                HyphaPiracea.id("off"),
                (stack, level, entity, i) -> entity instanceof Player player && player.getCooldowns().isOnCooldown(HyphaPiraceaItems.LEYFIELD_MAGNETOMETER) ? 1.0F : 0.0F
        );
    }

    public static void startClientTick() {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        Player player = client.player;

        LeyfieldEnvironmentEffects.update(level, client.cameraEntity);

        if(level != null) {
            if(player != null) {
                RandomSource randomSource = player.getRandom();

                // spawn magnetic field particles
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
                        if (s.isAir() || s.canBeReplaced()) {
                            Vec3 magField = HyphaPiraceaLevelAttachment.getAttachment(level).getMagneticFieldAtPosition(pos);
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

                // update highlight influenced blocks
                HitResult hitResult = client.hitResult;
                if(hitResult instanceof BlockHitResult bhr) {
                    if(level.getBlockEntity(bhr.getBlockPos()) instanceof ClientHighlightReactingBlockEntity chrbe) {
                        chrbe.onHighlight();
                    }
                }
            }
        }
    }

    public static void spawnFieldLine(Level level, Vec3 startPos, int length, double distance) {
        MagnetometerItem.spawnFieldLine(level, startPos, length, distance, (vec3, b) -> {
                    if (!b) {
                        RandomSource randomSource = level.random;
                        level.addParticle(HyphaPiraceaParticleTypes.FAIRY_FOG, true,
                                vec3.x + (randomSource.nextFloat() * 2.0 - 1.0) * 0.3,
                                vec3.y + (randomSource.nextFloat() * 2.0 - 1.0) * 0.3,
                                vec3.z + (randomSource.nextFloat() * 2.0 - 1.0) * 0.3,
                                (randomSource.nextFloat() * 2.0 - 1.0) * 0.05,
                                (randomSource.nextFloat() * 2.0 - 1.0) * 0.05,
                                (randomSource.nextFloat() * 2.0 - 1.0) * 0.05
                        );
                    }
                }
        );
    }

    public static void registerBlockColorHandlers(BlockColorHelper helper) {
        helper.register(((state, level, pos, tintIndex) -> {
            if(tintIndex == 0) {
                if(state.hasProperty(StormsapCellBlock.STORED_POWER)) {
                    int power = state.getValue(StormsapCellBlock.STORED_POWER);

                    float powerAmount = power / 15F;

                    Vec3 colorLow = new Vec3(0.35, 0.45, 0.5);
                    Vec3 colorHigh = new Vec3(0.65, 0.95, 1);
                    Vec3 lerp = colorLow.lerp(colorHigh, powerAmount);

                    int r = ((int)(lerp.x * 255)) & 0xFF;
                    int g = ((int)(lerp.y * 255)) & 0xFF;
                    int b = ((int)(lerp.z * 255)) & 0xFF;
                    return (r << 16) | (g << 8) | b;
                } else {
                    return -1;
                }
            } else {
                return -1;
            }
        }), HyphaPiraceaBlocks.STORMSAP_CELL);
    }

    @FunctionalInterface
    public interface BlockColorHelper {
        void register(BlockColor color, Block block);
    }

    public static void onClientStop(Minecraft client) {
        LeyfieldEnvironmentEffects.close();
    }
}
