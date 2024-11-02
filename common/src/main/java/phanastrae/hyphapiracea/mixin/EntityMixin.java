package phanastrae.hyphapiracea.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import phanastrae.hyphapiracea.duck.EntityDuckInterface;
import phanastrae.hyphapiracea.entity.HyphaPiraceaEntityAttachment;

@Mixin(Entity.class)
public class EntityMixin implements EntityDuckInterface {

    @Unique
    private HyphaPiraceaEntityAttachment hyphapiracea$attachment;

    @Override
    public HyphaPiraceaEntityAttachment hyphapiracea$getAttachment() {
        return hyphapiracea$attachment;
    }

    @Inject(method = "baseTick", at = @At("RETURN"))
    private void hyphapiracea$tick(CallbackInfo ci) {
        this.hyphapiracea$attachment.tick();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void hyphapiracea$init(EntityType type, Level world, CallbackInfo ci) {
        this.hyphapiracea$attachment = new HyphaPiraceaEntityAttachment((Entity)(Object)this);
    }
}
