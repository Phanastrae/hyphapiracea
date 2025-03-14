package phanastrae.hyphapiracea.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import phanastrae.hyphapiracea.HyphaPiracea;

public class HyphaPiraceaItemTags {
    public static final TagKey<Item> AZIMULDEY = create("azimuldey");
    public static final TagKey<Item> FAE_TOXIN = create("fae_toxin");
    public static final TagKey<Item> HYPHAL_CONDUCTOR = create("hyphal_conductor");

    private static TagKey<Item> create(String name) {
        return TagKey.create(Registries.ITEM, HyphaPiracea.id(name));
    }
}
