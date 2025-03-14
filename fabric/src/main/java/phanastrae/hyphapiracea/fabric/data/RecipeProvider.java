package phanastrae.hyphapiracea.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import phanastrae.hyphapiracea.item.HyphaPiraceaItemTags;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.world.item.Items.*;
import static phanastrae.hyphapiracea.item.HyphaPiraceaItems.*;

public class RecipeProvider extends FabricRecipeProvider  {
    public RecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void buildRecipes(RecipeOutput exporter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, AZIMULDEY_MASS, 24)
                .define('D', DIRT)
                .define('E', ENDER_PEARL)
                .define('R', ROTTEN_FLESH)
                .pattern("DRD")
                .pattern("RER")
                .pattern("DRD")
                .unlockedBy(
                        getHasName(ENDER_PEARL),
                        has(ENDER_PEARL)
                )
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, AZIMULIC_STEM, 8)
                .define('A', AZIMULDEY_MASS)
                .define('L', ItemTags.LOGS)
                .pattern("ALA")
                .pattern("ALA")
                .pattern("ALA")
                .unlockedBy(
                        getHasName(ENDER_PEARL),
                        has(ENDER_PEARL)
                )
                .save(exporter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, HYPHAL_NODE, 1)
                .requires(AZIMULDEY_MASS)
                .requires(HYPHALINE)
                .unlockedBy(
                        getHasName(HYPHALINE),
                        has(HYPHALINE)
                )
                .save(exporter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, HYPHAL_STEM, 1)
                .requires(AZIMULIC_STEM)
                .requires(HYPHALINE)
                .unlockedBy(
                        getHasName(HYPHALINE),
                        has(HYPHALINE)
                )
                .save(exporter);

        // conductors
        addConductor(exporter, HYPHAL_CONDUCTOR, AZIMULDEY_MASS);
        addConductor(exporter, OAK_CONDUCTOR, ItemTags.OAK_LOGS);
        addConductor(exporter, SPRUCE_CONDUCTOR, ItemTags.SPRUCE_LOGS);
        addConductor(exporter, BIRCH_CONDUCTOR, ItemTags.BIRCH_LOGS);
        addConductor(exporter, JUNGLE_CONDUCTOR, ItemTags.JUNGLE_LOGS);
        addConductor(exporter, ACACIA_CONDUCTOR, ItemTags.ACACIA_LOGS);
        addConductor(exporter, DARK_OAK_CONDUCTOR, ItemTags.DARK_OAK_LOGS);
        addConductor(exporter, MANGROVE_CONDUCTOR, ItemTags.MANGROVE_LOGS);
        addConductor(exporter, CHERRY_CONDUCTOR, ItemTags.CHERRY_LOGS);
        addConductor(exporter, BAMBOO_CONDUCTOR, ItemTags.BAMBOO_BLOCKS);
        addConductor(exporter, CRIMSON_CONDUCTOR, ItemTags.CRIMSON_STEMS);
        addConductor(exporter, WARPED_CONDUCTOR, ItemTags.WARPED_STEMS);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, STORMSAP_CELL, 1)
                .define('A', AZIMULDEY_MASS)
                .define('H', HYPHALINE)
                .define('G', GUNPOWDER)
                .pattern("AAA")
                .pattern("HGH")
                .pattern("AAA")
                .unlockedBy(
                        getHasName(HYPHALINE),
                        has(HYPHALINE)
                )
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, HYPHAL_AMMETER, 1)
                .define('A', AZIMULDEY_MASS)
                .define('H', HYPHALINE)
                .define('R', REDSTONE)
                .define('O', OGRAL_HYPHALINE)
                .pattern("ARA")
                .pattern("HOH")
                .pattern("AAA")
                .unlockedBy(
                        getHasName(HYPHALINE),
                        has(HYPHALINE)
                )
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, HYPHAL_VOLTMETER, 1)
                .define('A', AZIMULDEY_MASS)
                .define('H', HYPHALINE)
                .define('R', REDSTONE)
                .define('F', FERRIC_WARDLINE)
                .pattern("ARA")
                .pattern("HFH")
                .pattern("AAA")
                .unlockedBy(
                        getHasName(HYPHALINE),
                        has(HYPHALINE)
                )
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, CIRCUIT_SWITCH, 1)
                .define('A', AZIMULDEY_MASS)
                .define('H', HYPHALINE)
                .define('R', REDSTONE)
                .define('S', STICK)
                .pattern("ARA")
                .pattern("HSH")
                .pattern("AAA")
                .unlockedBy(
                        getHasName(HYPHALINE),
                        has(HYPHALINE)
                )
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, LEYFIELD_MAGNETOMETER_BLOCK, 3)
                .define('A', AZIMULDEY_MASS)
                .define('R', REDSTONE)
                .define('I', IRON_INGOT)
                .define('M', LEYFIELD_MAGNETOMETER)
                .pattern(" R ")
                .pattern("IMI")
                .pattern("AAA")
                .unlockedBy(
                        getHasName(LEYFIELD_MAGNETOMETER),
                        has(LEYFIELD_MAGNETOMETER)
                )
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ELECTROMAGNETIC_DUST_BOX, 1)
                .define('E', ELECTROMAGNETIC_DUST)
                .define('A', AZIMULDEY_MASS)
                .define('R', REDSTONE)
                .pattern("A A")
                .pattern("EEE")
                .pattern("ARA")
                .unlockedBy(
                        getHasName(ELECTROMAGNETIC_DUST),
                        has(ELECTROMAGNETIC_DUST)
                )
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, PIRACEATIC_LEUKBOX, 1)
                .define('H', HYPHALINE)
                .define('P', ENDER_PEARL)
                .define('B', BONE_BLOCK)
                .define('E', ELECTROMAGNETIC_DUST)
                .pattern("BEB")
                .pattern("HPH")
                .pattern("BEB")
                .unlockedBy(
                        getHasName(ELECTROMAGNETIC_DUST),
                        has(ELECTROMAGNETIC_DUST)
                )
                .save(exporter);

        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, HYPHALINE, 4)
                .define('A', AZIMULDEY_MASS)
                .define('S', STRING)
                .pattern(" S ")
                .pattern("SAS")
                .pattern(" S ")
                .unlockedBy(
                        getHasName(AZIMULDEY_MASS),
                        has(AZIMULDEY_MASS)
                )
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, OGRAL_HYPHALINE, 2)
                .define('C', COPPER_INGOT)
                .define('H', HYPHALINE)
                .pattern(" H ")
                .pattern("HCH")
                .pattern(" H ")
                .unlockedBy(
                        getHasName(HYPHALINE),
                        has(HYPHALINE)
                )
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, FERRIC_WARDLINE, 4)
                .define('I', IRON_INGOT)
                .define('H', HYPHALINE)
                .pattern(" H ")
                .pattern("HIH")
                .pattern(" H ")
                .unlockedBy(
                        getHasName(HYPHALINE),
                        has(HYPHALINE)
                )
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, LEYFIELD_MAGNETOMETER, 1)
                .define('C', HyphaPiraceaItemTags.HYPHAL_CONDUCTOR)
                .define('H', HYPHALINE)
                .define('R', REDSTONE)
                .define('I', IRON_INGOT)
                .pattern("H H")
                .pattern("CRC")
                .pattern(" I ")
                .unlockedBy(
                        getHasName(HYPHALINE),
                        has(HYPHALINE)
                )
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ELECTROMAGNETIC_DUST, 6)
                .define('A', AZIMULDEY_MASS)
                .define('R', REDSTONE)
                .pattern(" R ")
                .pattern("RAR")
                .pattern(" R ")
                .unlockedBy(
                        getHasName(HYPHALINE),
                        has(HYPHALINE)
                )
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, POSITIVE_SPOREBERRY, 3)
                .define('E', ELECTROMAGNETIC_DUST)
                .define('G', GLOW_BERRIES)
                .pattern(" E ")
                .pattern("EGE")
                .pattern(" E ")
                .unlockedBy(
                        getHasName(ELECTROMAGNETIC_DUST),
                        has(ELECTROMAGNETIC_DUST)
                )
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, NEGATIVE_SPOREBERRY, 3)
                .define('E', ELECTROMAGNETIC_DUST)
                .define('G', GLOW_BERRIES)
                .pattern("E E")
                .pattern(" G ")
                .pattern("E E")
                .unlockedBy(
                        getHasName(ELECTROMAGNETIC_DUST),
                        has(ELECTROMAGNETIC_DUST)
                )
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, NORTHERN_SPOREBERRY, 3)
                .define('E', ELECTROMAGNETIC_DUST)
                .define('G', GLOW_BERRIES)
                .pattern("E  ")
                .pattern("EGE")
                .pattern("  E")
                .unlockedBy(
                        getHasName(ELECTROMAGNETIC_DUST),
                        has(ELECTROMAGNETIC_DUST)
                )
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, SOUTHERN_SPOREBERRY, 3)
                .define('E', ELECTROMAGNETIC_DUST)
                .define('G', GLOW_BERRIES)
                .pattern(" EE")
                .pattern(" G ")
                .pattern("EE ")
                .unlockedBy(
                        getHasName(ELECTROMAGNETIC_DUST),
                        has(ELECTROMAGNETIC_DUST)
                )
                .save(exporter);
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, PIRACEATIC_GLOBGLASS, 1)
                .define('G', PIRACEATIC_GLOB)
                .pattern("GG")
                .pattern("GG")
                .unlockedBy(
                        getHasName(PIRACEATIC_GLOB),
                        has(PIRACEATIC_GLOB)
                )
                .save(exporter);


        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, POSITIVE_CHARGEBALL, 3)
                .requires(POSITIVE_SPOREBERRY)
                .requires(WIND_CHARGE)
                .requires(ELECTROMAGNETIC_DUST)
                .unlockedBy(
                        getHasName(POSITIVE_SPOREBERRY),
                        has(POSITIVE_SPOREBERRY)
                )
                .save(exporter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, NEGATIVE_CHARGEBALL, 3)
                .requires(NEGATIVE_SPOREBERRY)
                .requires(WIND_CHARGE)
                .requires(ELECTROMAGNETIC_DUST)
                .unlockedBy(
                        getHasName(NEGATIVE_SPOREBERRY),
                        has(NEGATIVE_SPOREBERRY)
                )
                .save(exporter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, NORTHERN_CHARGEBALL, 3)
                .requires(NORTHERN_SPOREBERRY)
                .requires(WIND_CHARGE)
                .requires(ELECTROMAGNETIC_DUST)
                .unlockedBy(
                        getHasName(NORTHERN_SPOREBERRY),
                        has(NORTHERN_SPOREBERRY)
                )
                .save(exporter);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, SOUTHERN_CHARGEBALL, 3)
                .requires(SOUTHERN_SPOREBERRY)
                .requires(WIND_CHARGE)
                .requires(ELECTROMAGNETIC_DUST)
                .unlockedBy(
                        getHasName(SOUTHERN_SPOREBERRY),
                        has(SOUTHERN_SPOREBERRY)
                )
                .save(exporter);
    }

    private static void addConductor(RecipeOutput exporter, Item conductor, TagKey<Item> itemTag) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, conductor, 6)
                .define('O', itemTag)
                .define('A', AZIMULDEY_MASS)
                .pattern("O")
                .pattern("A")
                .pattern("O")
                .unlockedBy(
                        getHasName(AZIMULDEY_MASS),
                        has(AZIMULDEY_MASS)
                )
                .save(exporter);
    }

    private static void addConductor(RecipeOutput exporter, Item conductor, ItemLike item) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, conductor, 6)
                .define('O', item)
                .define('A', AZIMULDEY_MASS)
                .pattern("O")
                .pattern("A")
                .pattern("O")
                .unlockedBy(
                        getHasName(AZIMULDEY_MASS),
                        has(AZIMULDEY_MASS)
                )
                .save(exporter);
    }
}
