package phanastrae.hyphapiracea.entity;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.block.entity.HyphalConductorBlockEntity;
import phanastrae.hyphapiracea.duck.EntityDuckInterface;

import java.util.List;

public class HyphaPiraceaEntityAttachment {

    private final Entity entity;
    private List<HyphalConductorBlockEntity> linkedConductors = new ObjectArrayList<>();

    public HyphaPiraceaEntityAttachment(Entity entity) {
        this.entity = entity;
    }

    public static HyphaPiraceaEntityAttachment getAttachment(Entity entity) {
        return ((EntityDuckInterface)entity).hyphapiracea$getAttachment();
    }

    public void tick() {
        if(!this.linkedConductors.isEmpty()) {
            this.linkedConductors.removeIf((BlockEntity::isRemoved));
        }
    }

    public void linkTo(HyphalConductorBlockEntity conductor) {
        this.linkedConductors.add(conductor);
    }

    public void unlinkTo(HyphalConductorBlockEntity conductor) {
        this.linkedConductors.removeIf(blockEntity -> blockEntity == conductor);
    }

    @Nullable
    public HyphalConductorBlockEntity getFirstLink() {
        if(this.linkedConductors.isEmpty()) {
            return null;
        } else {
            return this.linkedConductors.getFirst();
        }
    }
}
