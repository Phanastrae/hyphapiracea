package phanastrae.hyphapiracea.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;
import phanastrae.hyphapiracea.entity.ChargeballEntity;

public class ChargeballItem extends Item implements ProjectileItem {

    private final float electricCharge;
    private final float magneticCharge;

    public ChargeballItem(Properties properties, float electricCharge, float magneticCharge) {
        super(properties);

        this.electricCharge = electricCharge;
        this.magneticCharge = magneticCharge;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.PARROT_IMITATE_CREEPER,
                SoundSource.NEUTRAL,
                1.0F,
                1.5F / (level.getRandom().nextFloat() * 0.75F + 0.4F)
        );
        if (!level.isClientSide) {
            ChargeballEntity chargeball = new ChargeballEntity(player, level, player.getX(), player.getEyeY(), player.getZ());
            chargeball.setCharges(this.electricCharge, this.magneticCharge);
            chargeball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 0.5F, 1.0F);
            level.addFreshEntity(chargeball);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        itemstack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    @Override
    public DispenseConfig createDispenseConfig() {
        return ProjectileItem.DispenseConfig.builder()
                .positionFunction((blockSource, direction) -> DispenserBlock.getDispensePosition(blockSource, 1.0, Vec3.ZERO))
                .uncertainty(6.6666665F)
                .power(0.3F)
                .overrideDispenseEvent(1051)
                .build();
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        ChargeballEntity chargeball = new ChargeballEntity(level, pos.x(), pos.y(), pos.z(), Vec3.ZERO);
        chargeball.setCharges(this.electricCharge, this.magneticCharge);
        return chargeball;
    }
}
