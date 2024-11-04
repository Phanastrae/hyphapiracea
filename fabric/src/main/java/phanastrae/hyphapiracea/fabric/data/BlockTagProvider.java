package phanastrae.hyphapiracea.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import phanastrae.hyphapiracea.block.HyphaPiraceaBlockTags;
import phanastrae.hyphapiracea.block.HyphaPiraceaBlocks;

import java.util.concurrent.CompletableFuture;

public class BlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public BlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        getOrCreateTagBuilder(BlockTags.DRAGON_IMMUNE)
                .add(HyphaPiraceaBlocks.CREATIVE_CELL);

        getOrCreateTagBuilder(BlockTags.WITHER_IMMUNE)
                .add(HyphaPiraceaBlocks.CREATIVE_CELL);

        getOrCreateTagBuilder(BlockTags.MUSHROOM_GROW_BLOCK)
                .add(HyphaPiraceaBlocks.AZIMULDEY_MASS)
                .add(HyphaPiraceaBlocks.AZIMULIC_STEM);

        getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_AXE)
                .add(HyphaPiraceaBlocks.HYPHAL_CONDUCTOR)
                .add(HyphaPiraceaBlocks.STORMSAP_CELL)
                .add(HyphaPiraceaBlocks.CREATIVE_CELL)
                .add(HyphaPiraceaBlocks.HYPHAL_AMMETER)
                .add(HyphaPiraceaBlocks.HYPHAL_VOLTMETER)
                .add(HyphaPiraceaBlocks.CIRCUIT_SWITCH)
                .add(HyphaPiraceaBlocks.ELECTROMAGNETIC_DUST_BOX)
                .add(HyphaPiraceaBlocks.PIRACEATIC_LEUKBOX);

        getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_HOE)
                .add(HyphaPiraceaBlocks.AZIMULDEY_MASS)
                .add(HyphaPiraceaBlocks.AZIMULIC_STEM)
                .add(HyphaPiraceaBlocks.HYPHAL_NODE)
                .add(HyphaPiraceaBlocks.HYPHAL_STEM)
                .add(HyphaPiraceaBlocks.HYPHAL_CONDUCTOR);

        getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(HyphaPiraceaBlocks.STORMSAP_CELL)
                .add(HyphaPiraceaBlocks.CREATIVE_CELL)
                .add(HyphaPiraceaBlocks.HYPHAL_AMMETER)
                .add(HyphaPiraceaBlocks.HYPHAL_VOLTMETER)
                .add(HyphaPiraceaBlocks.CIRCUIT_SWITCH)
                .add(HyphaPiraceaBlocks.LEYFIELD_MAGNETOMETER_BLOCK)
                .add(HyphaPiraceaBlocks.ELECTROMAGNETIC_DUST_BOX)
                .add(HyphaPiraceaBlocks.PIRACEATIC_LEUKBOX);

        getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(HyphaPiraceaBlocks.AZIMULDEY_MASS)
                .add(HyphaPiraceaBlocks.AZIMULIC_STEM)
                .add(HyphaPiraceaBlocks.HYPHAL_NODE)
                .add(HyphaPiraceaBlocks.HYPHAL_STEM)
                .add(HyphaPiraceaBlocks.HYPHAL_CONDUCTOR)
                .add(HyphaPiraceaBlocks.PIRACEATIC_TAR)
                .add(HyphaPiraceaBlocks.PIRACEATIC_GLOBGLASS);

        getOrCreateTagBuilder(ConventionalBlockTags.RELOCATION_NOT_SUPPORTED)
                .add(HyphaPiraceaBlocks.HYPHAL_CONDUCTOR)
                .add(HyphaPiraceaBlocks.PIRACEATIC_LEUKBOX);

        getOrCreateTagBuilder(HyphaPiraceaBlockTags.PLACEMENT_FRAGILE);

        getOrCreateTagBuilder(HyphaPiraceaBlockTags.NOT_FEASTABLE)
                .add(Blocks.OBSIDIAN)
                .add(Blocks.CRYING_OBSIDIAN)
                .addTag(HyphaPiraceaBlockTags.FAE_TOXIN);

        getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_RAW_IRON);
        getOrCreateTagBuilder(ConventionalBlockTags.STORAGE_BLOCKS_IRON);
        getOrCreateTagBuilder(BlockTags.IRON_ORES);

        getOrCreateTagBuilder(HyphaPiraceaBlockTags.FAE_TOXIN)
                .addTag(ConventionalBlockTags.STORAGE_BLOCKS_IRON)
                .addTag(ConventionalBlockTags.STORAGE_BLOCKS_RAW_IRON)
                .addTag(BlockTags.IRON_ORES)
                .add(Blocks.IRON_BARS)
                .add(Blocks.IRON_DOOR)
                .add(Blocks.IRON_TRAPDOOR)
                .add(Blocks.ANVIL)
                .add(Blocks.CHIPPED_ANVIL)
                .add(Blocks.DAMAGED_ANVIL);
    }
}
