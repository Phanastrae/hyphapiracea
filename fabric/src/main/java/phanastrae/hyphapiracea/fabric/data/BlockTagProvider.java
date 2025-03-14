package phanastrae.hyphapiracea.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
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
        TagKey<Block> STORAGE_BLOCKS_STEEL = createConventional("storage_blocks/steel");
        TagKey<Block> STORAGE_BLOCKS_RAW_STEEL = createConventional("storage_blocks/raw_steel");
        TagKey<Block> STEEL_ORES = createConventional("ores/steel");

        // vanilla tags
        getOrCreateTagBuilder(BlockTags.DRAGON_IMMUNE)
                .add(HyphaPiraceaBlocks.CREATIVE_CELL);

        getOrCreateTagBuilder(BlockTags.WITHER_IMMUNE)
                .add(HyphaPiraceaBlocks.CREATIVE_CELL);

        getOrCreateTagBuilder(BlockTags.MUSHROOM_GROW_BLOCK)
                .add(HyphaPiraceaBlocks.AZIMULDEY_MASS)
                .add(HyphaPiraceaBlocks.AZIMULIC_STEM);

        getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_AXE)
                .add(HyphaPiraceaBlocks.STORMSAP_CELL)
                .add(HyphaPiraceaBlocks.CREATIVE_CELL)
                .add(HyphaPiraceaBlocks.HYPHAL_AMMETER)
                .add(HyphaPiraceaBlocks.HYPHAL_VOLTMETER)
                .add(HyphaPiraceaBlocks.CIRCUIT_SWITCH)
                .add(HyphaPiraceaBlocks.ELECTROMAGNETIC_DUST_BOX)
                .add(HyphaPiraceaBlocks.PIRACEATIC_LEUKBOX)
                .addOptionalTag(HyphaPiraceaBlockTags.HYPHAL_CONDUCTOR);

        getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_HOE)
                .add(HyphaPiraceaBlocks.AZIMULDEY_MASS)
                .add(HyphaPiraceaBlocks.AZIMULIC_STEM)
                .add(HyphaPiraceaBlocks.HYPHAL_NODE)
                .add(HyphaPiraceaBlocks.HYPHAL_STEM)
                .addOptionalTag(HyphaPiraceaBlockTags.HYPHAL_CONDUCTOR);

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
                .add(HyphaPiraceaBlocks.PIRACEATIC_TAR)
                .add(HyphaPiraceaBlocks.PIRACEATIC_GLOBGLASS)
                .addOptionalTag(HyphaPiraceaBlockTags.HYPHAL_CONDUCTOR);

        // convention tags
        getOrCreateTagBuilder(ConventionalBlockTags.RELOCATION_NOT_SUPPORTED)
                .add(HyphaPiraceaBlocks.PIRACEATIC_LEUKBOX)
                .addOptionalTag(HyphaPiraceaBlockTags.HYPHAL_CONDUCTOR);

        // hyphapiracea tags
        getOrCreateTagBuilder(HyphaPiraceaBlockTags.AZIMULDEY)
                .add(HyphaPiraceaBlocks.AZIMULDEY_MASS)
                .add(HyphaPiraceaBlocks.AZIMULIC_STEM)
                .add(HyphaPiraceaBlocks.HYPHAL_NODE)
                .add(HyphaPiraceaBlocks.HYPHAL_STEM)
                .add(HyphaPiraceaBlocks.STORMSAP_CELL)
                .add(HyphaPiraceaBlocks.CREATIVE_CELL)
                .add(HyphaPiraceaBlocks.HYPHAL_AMMETER)
                .add(HyphaPiraceaBlocks.HYPHAL_VOLTMETER)
                .add(HyphaPiraceaBlocks.CIRCUIT_SWITCH)
                .add(HyphaPiraceaBlocks.LEYFIELD_MAGNETOMETER_BLOCK)
                .add(HyphaPiraceaBlocks.ELECTROMAGNETIC_DUST_BOX)
                .add(HyphaPiraceaBlocks.PIRACEATIC_LEUKBOX)
                .addOptionalTag(HyphaPiraceaBlockTags.HYPHAL_CONDUCTOR);

        getOrCreateTagBuilder(HyphaPiraceaBlockTags.FAE_TOXIN)
                .addOptionalTag(ConventionalBlockTags.STORAGE_BLOCKS_IRON)
                .addOptionalTag(ConventionalBlockTags.STORAGE_BLOCKS_RAW_IRON)
                .addOptionalTag(BlockTags.IRON_ORES)
                .addOptionalTag(STORAGE_BLOCKS_STEEL)
                .addOptionalTag(STORAGE_BLOCKS_RAW_STEEL)
                .addOptionalTag(STEEL_ORES)
                .add(Blocks.IRON_BARS)
                .add(Blocks.IRON_DOOR)
                .add(Blocks.IRON_TRAPDOOR)
                .add(Blocks.ANVIL)
                .add(Blocks.CHIPPED_ANVIL)
                .add(Blocks.DAMAGED_ANVIL);

        getOrCreateTagBuilder(HyphaPiraceaBlockTags.PIRACEATIC)
                .add(HyphaPiraceaBlocks.PIRACEATIC_TAR)
                .add(HyphaPiraceaBlocks.PIRACEATIC_GLOBGLASS);

        getOrCreateTagBuilder(HyphaPiraceaBlockTags.PLACEMENT_FRAGILE);

        getOrCreateTagBuilder(HyphaPiraceaBlockTags.NOT_FEASTABLE)
                .addTag(HyphaPiraceaBlockTags.AZIMULDEY)
                .addTag(HyphaPiraceaBlockTags.FAE_TOXIN)
                .addTag(HyphaPiraceaBlockTags.PIRACEATIC)
                .add(Blocks.OBSIDIAN)
                .add(Blocks.CRYING_OBSIDIAN);

        // conductors
        for(Block conductor : HyphaPiraceaBlocks.CONDUCTORS) {
            getOrCreateTagBuilder(HyphaPiraceaBlockTags.HYPHAL_CONDUCTOR)
                    .add(conductor);
        }
    }

    private static TagKey<Block> createConventional(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", name));
    }
}
