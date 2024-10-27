package phanastrae.ywsanf.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
        Vec3 pos = player.getEyePosition();
        Vec3 viewVector = player.getViewVector(1);

        for(int i = 0; i < 4; i++) {
            Vec3 p = pos.add(viewVector.scale(i * 2.5 + 1.0));
            int n = 650 - 100 * i;
            double d = 0.05 + 0.015 * i;
            spawnFieldLine(level, p, n, d);
        }
        RandomSource randomSource = level.getRandom();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.PLAYERS, 1.0F, 1.4F + randomSource.nextFloat() * 0.3F);

        player.getCooldowns().addCooldown(this, 5);

        return super.use(level, player, usedHand);
    }

    public void spawnFieldLine(Level level, Vec3 startPos, int length, double distance) {
        Vec3 pos = startPos;
        YWSaNFLevelAttachment yla = YWSaNFLevelAttachment.getAttachment(level);
        for(int i = 0; i < length * 2; i++) {
            if(i == length) {
                pos = startPos;
            }

            Vec3 magneticField = yla.getMagneticFieldAtPosition(pos);
            pos = pos.add(magneticField.scale((1 / magneticField.length()) * distance * (i >= length ? -1 : 1)));

            level.addParticle(YWSaNFParticleTypes.LINE_SPECK, true,
                    pos.x, pos.y, pos.z,
                    0, 0, 0);
        }
    }
}
