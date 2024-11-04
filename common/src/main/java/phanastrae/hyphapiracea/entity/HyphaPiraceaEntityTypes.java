package phanastrae.hyphapiracea.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.services.XPlatInterface;

import java.util.function.BiConsumer;

public class HyphaPiraceaEntityTypes {

    public static final ResourceLocation CHARGEBALL_KEY = id("chargeball");
    public static final EntityType<ChargeballEntity> CHARGEBALL =
            EntityType.Builder.<ChargeballEntity>of(ChargeballEntity::new, MobCategory.MISC)
                    .sized(0.3125F, 0.3125F)
                    .eyeHeight(0.0F)
                    .clientTrackingRange(4)
                    .updateInterval(5)
                    .build(getStr(CHARGEBALL_KEY));

    public static void init(BiConsumer<ResourceLocation, EntityType<?>> r) {
        r.accept(CHARGEBALL_KEY, CHARGEBALL);
    }

    private static ResourceLocation id(String path) {
        return HyphaPiracea.id(path);
    }

    @Nullable
    private static String getStr(ResourceLocation resourceLocation) {
        // sending null on neoforge crashes, but sending a string on fabric logs an error
        String loader = XPlatInterface.INSTANCE.getLoader();
        if(loader.equals("fabric")) {
            return null;
        } else {
            return resourceLocation.toString();
        }
    }
}
