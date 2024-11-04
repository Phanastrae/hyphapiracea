package phanastrae.hyphapiracea.item;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import phanastrae.hyphapiracea.particle.HyphaPiraceaParticleTypes;
import phanastrae.hyphapiracea.world.HyphaPiraceaLevelAttachment;

import java.util.function.BiConsumer;

public class MagnetometerItem extends Item {

    public MagnetometerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        RandomSource randomSource = level.getRandom();
        player.playSound(SoundEvents.LEVER_CLICK, 1.0F, 1.7F + randomSource.nextFloat() * 0.2F);
        return ItemUtils.startUsingInstantly(level, player, usedHand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        Vec3 pos = livingEntity.getEyePosition();
        Vec3 viewVector = livingEntity.getViewVector(1);

        for(int i = 0; i < 4; i++) {
            Vec3 p = pos.add(viewVector.scale(i * 2.5 + 1.0));
            int n = 650 - 100 * i;
            double d = 0.05 + 0.015 * i;
            spawnFieldLine(level, p, n, d);
        }
        RandomSource randomSource = level.getRandom();
        level.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.PLAYERS, 1.0F, 1.4F + randomSource.nextFloat() * 0.3F);

        if(livingEntity instanceof Player player) {
            player.getCooldowns().addCooldown(this, 20);
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        return super.finishUsingItem(stack, level, livingEntity);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 15;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BRUSH;
    }

    public static void spawnFieldLine(Level level, Vec3 startPos, int length, double distance) {
        spawnFieldLine(level, startPos, length, distance, (vec3, b) -> {
                    RandomSource random = level.random;
                    float f = random.nextFloat();

                    ParticleOptions particleOptions;
                    if(b) {
                        if (f > 0.85) {
                            particleOptions = HyphaPiraceaParticleTypes.LINE_SPECK;
                        } else if (f > 0.8) {
                            particleOptions = HyphaPiraceaParticleTypes.ZAPPY_GRIT;
                        } else {
                            particleOptions = ParticleTypes.MYCELIUM;
                        }
                    } else {
                        if (f > 0.1) {
                            particleOptions = HyphaPiraceaParticleTypes.LINE_SPECK;
                        } else if (f > 0.05) {
                            particleOptions = HyphaPiraceaParticleTypes.ZAPPY_GRIT;
                        } else {
                            particleOptions = HyphaPiraceaParticleTypes.FAIRY_FOG;
                        }
                    }
                    level.addParticle(
                            particleOptions, true,
                            vec3.x, vec3.y, vec3.z,
                            0, 0, 0
                    );
                }
        );
    }

    public static void spawnFieldLine(Level level, Vec3 startPos, int length, double distance, BiConsumer<Vec3, Boolean> particleSpawner) {
        Vec3 pos = startPos;
        HyphaPiraceaLevelAttachment yla = HyphaPiraceaLevelAttachment.getAttachment(level);
        for(int i = 0; i < length * 2; i++) {
            if(i == length) {
                pos = startPos;
            }

            float progress = (i % length) / (float)length;
            double d = distance / (1 - progress * progress);
            int sign = (i >= length ? -1 : 1);

            Vec3 magneticField = yla.getMagneticFieldAtPosition(pos);
            boolean warded = yla.isPositionWarded(pos);
            double oneByLength = 1 / magneticField.length();
            pos = pos.add(magneticField.scale(oneByLength * sign * d));

            particleSpawner.accept(pos, warded);
        }
    }
}
