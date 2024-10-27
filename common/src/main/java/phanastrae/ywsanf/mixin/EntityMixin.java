package phanastrae.ywsanf.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.ywsanf.duck.EntityDuckInterface;
import phanastrae.ywsanf.entity.YWSaNFEntityAttachment;

@Mixin(Entity.class)
public class EntityMixin implements EntityDuckInterface {

    @Unique
    private YWSaNFEntityAttachment ywsanf$attachment;

    @Override
    public YWSaNFEntityAttachment ywsanf$getAttachment() {
        return ywsanf$attachment;
    }

    @Inject(method = "baseTick", at = @At("RETURN"))
    private void ywsanf$tick(CallbackInfo ci) {
        this.ywsanf$attachment.tick();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ywsanf$init(EntityType type, Level world, CallbackInfo ci) {
        this.ywsanf$attachment = new YWSaNFEntityAttachment((Entity)(Object)this);
    }
}
