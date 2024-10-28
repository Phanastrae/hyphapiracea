package phanastrae.ywsanf.item;

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
import phanastrae.ywsanf.particle.YWSaNFParticleTypes;
import phanastrae.ywsanf.world.YWSaNFLevelAttachment;

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
        Vec3 pos = startPos;
        YWSaNFLevelAttachment yla = YWSaNFLevelAttachment.getAttachment(level);
        for(int i = 0; i < length * 2; i++) {
            if(i == length) {
                pos = startPos;
            }

            float progress = (i % length) / (float)length;
            double d = distance / (1 - progress * progress);
            int sign = (i >= length ? -1 : 1);

            Vec3 magneticField = yla.getMagneticFieldAtPosition(pos);
            double oneByLength = 1 / magneticField.length();
            pos = pos.add(magneticField.scale(oneByLength * sign * d));

            level.addParticle(YWSaNFParticleTypes.LINE_SPECK, true,
                    pos.x, pos.y, pos.z,
                    0, 0, 0);
        }
    }
}
