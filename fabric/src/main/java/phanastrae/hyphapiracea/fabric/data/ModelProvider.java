package phanastrae.hyphapiracea.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.block.HyphaPiraceaBlocks;
import phanastrae.hyphapiracea.item.HyphaPiraceaItems;

import static net.minecraft.core.Direction.*;
import static net.minecraft.data.models.blockstates.VariantProperties.*;
import static net.minecraft.data.models.blockstates.VariantProperties.Rotation.*;
import static net.minecraft.data.models.model.TextureSlot.ALL;
import static net.minecraft.data.models.model.TextureSlot.PARTICLE;

public class ModelProvider extends FabricModelProvider {
    public ModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators BMG) {
        BMG.createTrivialCube(HyphaPiraceaBlocks.LEUKBOX);
        BMG.createTrivialCube(HyphaPiraceaBlocks.FEASTING_TAR);
        BMG.createTrivialCube(HyphaPiraceaBlocks.GALVANOCARPIC_BULB);

        this.createTopSideBottom(BMG, HyphaPiraceaBlocks.MAGNETOMETER_BLOCK);

        this.createCubeTopBottomSide(BMG, HyphaPiraceaBlocks.VOLTMETER_BLOCK);
        this.createCubeTopBottomSide(BMG, HyphaPiraceaBlocks.AMMETER_BLOCK);
        this.createCubeTopBottomSideWithTintedSides(BMG, HyphaPiraceaBlocks.STORMSAP_CELL);

        createConductorBlock(BMG, HyphaPiraceaBlocks.HYPHAL_CONDUCTOR);
    }

    @Override
    public void generateItemModels(ItemModelGenerators IMG) {
        generateFlat(IMG,
                HyphaPiraceaItems.KEYED_DISC,
                HyphaPiraceaItems.HYPHALINE
        );
    }

    private static void generateFlat(ItemModelGenerators IMG, Item... items) {
        for(Item item : items) {
            generateFlat(IMG, item);
        }
    }

    private static void generateFlat(ItemModelGenerators IMG, Item item) {
        IMG.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
    }

    public static void createConductorBlock(BlockModelGenerators BMG, Block block) {
        TextureMapping verticalMapping = new TextureMapping()
                .put(PARTICLE, getBlockTexture(Blocks.POLISHED_DEEPSLATE))
                .put(ALL, getBlockTexture(Blocks.POLISHED_DEEPSLATE));
        ResourceLocation verticalModel = HyphaPiraceaModelTemplates.CONDUCTOR.create(block, verticalMapping, BMG.modelOutput);

        TextureMapping horizontalMapping = new TextureMapping()
                .put(PARTICLE, getBlockTexture(Blocks.POLISHED_DEEPSLATE))
                .put(ALL, getBlockTexture(Blocks.POLISHED_DEEPSLATE));
        ResourceLocation horizontalModel = HyphaPiraceaModelTemplates.CONDUCTOR_WALL.create(block, horizontalMapping, BMG.modelOutput);

        BMG.blockStateOutput
                .accept(
                        MultiVariantGenerator.multiVariant(block)
                                .with(
                                        PropertyDispatch.property(BlockStateProperties.FACING)
                                                .select(UP, variant().with(MODEL, verticalModel))
                                                .select(DOWN, variant().with(MODEL, verticalModel).with(X_ROT, R180))
                                                .select(NORTH, variant().with(MODEL, horizontalModel))
                                                .select(EAST, variant().with(MODEL, horizontalModel).with(Y_ROT, R90))
                                                .select(SOUTH, variant().with(MODEL, horizontalModel).with(Y_ROT, R180))
                                                .select(WEST, variant().with(MODEL, horizontalModel).with(Y_ROT, R270))
                                )
                );
    }

    private void createTopSideBottom(BlockModelGenerators BMG, Block block) {
        TextureMapping textureMapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(block, "_bottom"))
                .put(TextureSlot.UP, TextureMapping.getBlockTexture(block, "_top"))
                .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.EAST, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.WEST, TextureMapping.getBlockTexture(block, "_side"));
        BMG.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, ModelTemplates.CUBE.create(block, textureMapping, BMG.modelOutput)));
    }

    public void createCubeTopBottomSide(BlockModelGenerators BMG, Block block) {
        ResourceLocation positiveTerminal = HyphaPiracea.id("block/hyphal_positive_terminal");
        ResourceLocation negativeTerminal = HyphaPiracea.id("block/hyphal_negative_terminal");
        TextureMapping verticalMapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.BOTTOM, negativeTerminal)
                .put(TextureSlot.TOP, positiveTerminal)
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side"));
        ResourceLocation verticalModel = HyphaPiraceaModelTemplates.CUBE_TOP_BOTTOM_SIDE.create(block, verticalMapping, BMG.modelOutput);

        BMG.blockStateOutput
                .accept(
                        MultiVariantGenerator.multiVariant(block)
                                .with(
                                        PropertyDispatch.property(BlockStateProperties.FACING)
                                                .select(
                                                        DOWN, Variant.variant().with(MODEL, verticalModel).with(X_ROT, R180)
                                                )
                                                .select(UP, Variant.variant().with(MODEL, verticalModel))
                                                .select(
                                                        NORTH, Variant.variant().with(MODEL, verticalModel).with(X_ROT, R90)
                                                )
                                                .select(
                                                        SOUTH, Variant.variant().with(MODEL, verticalModel).with(X_ROT, R90).with(Y_ROT, R180)
                                                )
                                                .select(
                                                        EAST, Variant.variant().with(MODEL, verticalModel).with(X_ROT, R90).with(Y_ROT, R90)
                                                )
                                                .select(
                                                        WEST, Variant.variant().with(MODEL, verticalModel).with(X_ROT, R90).with(Y_ROT, R270)
                                                )
                                )
                );
    }

    public void createCubeTopBottomSideWithTintedSides(BlockModelGenerators BMG, Block block) {
        ResourceLocation positiveTerminal = HyphaPiracea.id("block/hyphal_positive_terminal");
        ResourceLocation negativeTerminal = HyphaPiracea.id("block/hyphal_negative_terminal");
        TextureMapping verticalMapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.BOTTOM, negativeTerminal)
                .put(TextureSlot.TOP, positiveTerminal)
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side"))
                .put(HyphaPiraceaModelTemplates.TINT_SIDE, TextureMapping.getBlockTexture(block, "_side_tint"));
        ResourceLocation verticalModel = HyphaPiraceaModelTemplates.CUBE_TOP_BOTTOM_SIDE_TINTED_SIDES.create(block, verticalMapping, BMG.modelOutput);

        BMG.blockStateOutput
                .accept(
                        MultiVariantGenerator.multiVariant(block)
                                .with(
                                        PropertyDispatch.property(BlockStateProperties.FACING)
                                                .select(
                                                        DOWN, Variant.variant().with(MODEL, verticalModel).with(X_ROT, R180)
                                                )
                                                .select(UP, Variant.variant().with(MODEL, verticalModel))
                                                .select(
                                                        NORTH, Variant.variant().with(MODEL, verticalModel).with(X_ROT, R90)
                                                )
                                                .select(
                                                        SOUTH, Variant.variant().with(MODEL, verticalModel).with(X_ROT, R90).with(Y_ROT, R180)
                                                )
                                                .select(
                                                        EAST, Variant.variant().with(MODEL, verticalModel).with(X_ROT, R90).with(Y_ROT, R90)
                                                )
                                                .select(
                                                        WEST, Variant.variant().with(MODEL, verticalModel).with(X_ROT, R90).with(Y_ROT, R270)
                                                )
                                )
                );
    }

    public static Variant variant() {
        return Variant.variant();
    }

    public static ResourceLocation getBlockTexture(Block block) {
        return TextureMapping.getBlockTexture(block);
    }

    public static ResourceLocation getBlockTexture(Block block, String textureSuffix) {
        return TextureMapping.getBlockTexture(block, textureSuffix);
    }
}
