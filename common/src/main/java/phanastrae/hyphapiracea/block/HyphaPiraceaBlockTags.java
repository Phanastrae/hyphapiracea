package phanastrae.hyphapiracea.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import phanastrae.hyphapiracea.HyphaPiracea;

public class HyphaPiraceaBlockTags {
    public static final TagKey<Block> AZIMULDEY = create("azimuldey");
    public static final TagKey<Block> FAE_TOXIN = create("fae_toxin");
    public static final TagKey<Block> NOT_FEASTABLE = create("not_feastable");
    public static final TagKey<Block> PIRACEATIC = create("piraceatic");
    public static final TagKey<Block> PLACEMENT_FRAGILE = create("placement_fragile");

    private static TagKey<Block> create(String name) {
        return TagKey.create(Registries.BLOCK, HyphaPiracea.id(name));
    }
}
