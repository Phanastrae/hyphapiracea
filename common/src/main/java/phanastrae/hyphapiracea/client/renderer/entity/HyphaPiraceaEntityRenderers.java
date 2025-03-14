package phanastrae.hyphapiracea.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import phanastrae.hyphapiracea.entity.HyphaPiraceaEntityTypes;

public class HyphaPiraceaEntityRenderers {

    public static void init(EntityRendererAcceptor r) {
        r.accept(HyphaPiraceaEntityTypes.CHARGEBALL, ChargeballEntityRenderer::new);
    }

    @FunctionalInterface
    public interface EntityRendererAcceptor {
        <T extends Entity> void accept(EntityType<? extends T> type, EntityRendererProvider<T> entityRendererProvider);
    }
}
