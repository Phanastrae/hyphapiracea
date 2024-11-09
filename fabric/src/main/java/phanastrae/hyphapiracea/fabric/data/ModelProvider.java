package phanastrae.hyphapiracea.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.*;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.block.HyphaPiraceaBlocks;
import phanastrae.hyphapiracea.block.LeukboxBlock;
import phanastrae.hyphapiracea.item.HyphaPiraceaItems;

import static net.minecraft.core.Direction.DOWN;
import static net.minecraft.core.Direction.EAST;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.core.Direction.UP;
import static net.minecraft.core.Direction.WEST;
import static net.minecraft.data.models.BlockModelGenerators.createHorizontalFacingDispatch;
import static net.minecraft.data.models.blockstates.VariantProperties.*;
import static net.minecraft.data.models.blockstates.VariantProperties.Rotation.*;
import static net.minecraft.data.models.model.TextureSlot.*;

public class ModelProvider extends FabricModelProvider {
    public ModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators BMG) {
        BMG.createTrivialCube(HyphaPiraceaBlocks.PIRACEATIC_TAR);
        BMG.createTrivialCube(HyphaPiraceaBlocks.PIRACEATIC_GLOBGLASS);

        this.createTopSideBottom(BMG, HyphaPiraceaBlocks.LEYFIELD_MAGNETOMETER_BLOCK);

        BMG.createAxisAlignedPillarBlock(HyphaPiraceaBlocks.AZIMULIC_STEM, TexturedModel.COLUMN);
        BMG.createAxisAlignedPillarBlock(HyphaPiraceaBlocks.HYPHAL_STEM, TexturedModel.COLUMN);

        this.createCubeTopBottomSide(BMG, HyphaPiraceaBlocks.HYPHAL_VOLTMETER);
        this.createCubeTopBottomSide(BMG, HyphaPiraceaBlocks.HYPHAL_AMMETER);
        this.createCubeTopBottomSide(BMG, HyphaPiraceaBlocks.CREATIVE_CELL, HyphaPiracea.id("block/creative_hyphal_positive_terminal"), HyphaPiracea.id("block/creative_hyphal_negative_terminal"));
        this.createCubeTopBottomSideWithTintedSides(BMG, HyphaPiraceaBlocks.STORMSAP_CELL);
        this.createElectromagneticDustBox(BMG, HyphaPiraceaBlocks.ELECTROMAGNETIC_DUST_BOX);

        this.createCircuitSwitch(BMG, HyphaPiraceaBlocks.CIRCUIT_SWITCH, HyphaPiracea.id("block/hyphal_positive_terminal"), HyphaPiracea.id("block/hyphal_negative_terminal"));

        this.createHyphalNode(BMG, HyphaPiraceaBlocks.AZIMULDEY_MASS);
        this.createHyphalNode(BMG, HyphaPiraceaBlocks.HYPHAL_NODE);

        this.createConductorBlock(BMG, HyphaPiraceaBlocks.HYPHAL_CONDUCTOR);

        this.createLeukbox(BMG, HyphaPiraceaBlocks.PIRACEATIC_LEUKBOX);
    }

    @Override
    public void generateItemModels(ItemModelGenerators IMG) {
        generateFlat(IMG,
                HyphaPiraceaItems.KEYED_DISC,
                HyphaPiraceaItems.HYPHALINE,
                HyphaPiraceaItems.OGRAL_HYPHALINE,
                HyphaPiraceaItems.FERRIC_WARDLINE,
                HyphaPiraceaItems.ELECTROMAGNETIC_DUST,
                HyphaPiraceaItems.POSITIVE_CHARGEBALL,
                HyphaPiraceaItems.NEGATIVE_CHARGEBALL,
                HyphaPiraceaItems.NORTHERN_CHARGEBALL,
                HyphaPiraceaItems.SOUTHERN_CHARGEBALL,
                HyphaPiraceaItems.POSITIVE_SPOREBERRY,
                HyphaPiraceaItems.NEGATIVE_SPOREBERRY,
                HyphaPiraceaItems.NORTHERN_SPOREBERRY,
                HyphaPiraceaItems.SOUTHERN_SPOREBERRY,
                HyphaPiraceaItems.PIRACEATIC_GLOB
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

    public void createConductorBlock(BlockModelGenerators BMG, Block block) {
        TextureMapping verticalMapping = new TextureMapping()
                .put(PARTICLE, getBlockTexture(HyphaPiraceaBlocks.AZIMULDEY_MASS))
                .put(ALL, getBlockTexture(HyphaPiraceaBlocks.AZIMULDEY_MASS));
        ResourceLocation verticalModel = HyphaPiraceaModelTemplates.CONDUCTOR.create(block, verticalMapping, BMG.modelOutput);

        TextureMapping horizontalMapping = new TextureMapping()
                .put(PARTICLE, getBlockTexture(HyphaPiraceaBlocks.AZIMULDEY_MASS))
                .put(ALL, getBlockTexture(HyphaPiraceaBlocks.AZIMULDEY_MASS));
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
        this.createCubeTopBottomSide(BMG, block, positiveTerminal, negativeTerminal);
    }

    public void createCubeTopBottomSide(BlockModelGenerators BMG, Block block, ResourceLocation top, ResourceLocation bottom) {
        TextureMapping verticalMapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.BOTTOM, bottom)
                .put(TextureSlot.TOP, top)
                .put(SIDE, TextureMapping.getBlockTexture(block, "_side"));
        ResourceLocation verticalModel = ModelTemplates.CUBE_BOTTOM_TOP.create(block, verticalMapping, BMG.modelOutput);

        BMG.blockStateOutput
                .accept(
                        MultiVariantGenerator.multiVariant(block)
                                .with(
                                        PropertyDispatch.property(BlockStateProperties.FACING)
                                                .select(
                                                        DOWN, Variant.variant().with(MODEL, verticalModel).with(X_ROT, R180)
                                                )
                                                .select(
                                                        UP, Variant.variant().with(MODEL, verticalModel)
                                                )
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

    public void createCircuitSwitch(BlockModelGenerators BMG, Block block, ResourceLocation top, ResourceLocation bottom) {
        TextureMapping offMapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.BOTTOM, bottom)
                .put(TextureSlot.TOP, top)
                .put(SIDE, TextureMapping.getBlockTexture(block, "_side"));
        ResourceLocation offModel = ModelTemplates.CUBE_BOTTOM_TOP.create(block, offMapping, BMG.modelOutput);

        TextureMapping onMapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_side_on"))
                .put(TextureSlot.BOTTOM, bottom)
                .put(TextureSlot.TOP, top)
                .put(SIDE, TextureMapping.getBlockTexture(block, "_side_on"));
        ResourceLocation onModel = ModelTemplates.CUBE_BOTTOM_TOP.createWithOverride(block, "_on", onMapping, BMG.modelOutput);

        BMG.blockStateOutput
                .accept(
                        MultiVariantGenerator.multiVariant(block)
                                .with(
                                        PropertyDispatch.properties(BlockStateProperties.FACING, BlockStateProperties.POWERED)
                                                .select(
                                                        DOWN, false, Variant.variant().with(MODEL, offModel).with(X_ROT, R180)
                                                )
                                                .select(
                                                        UP, false, Variant.variant().with(MODEL, offModel)
                                                )
                                                .select(
                                                        NORTH, false, Variant.variant().with(MODEL, offModel).with(X_ROT, R90)
                                                )
                                                .select(
                                                        SOUTH, false, Variant.variant().with(MODEL, offModel).with(X_ROT, R90).with(Y_ROT, R180)
                                                )
                                                .select(
                                                        EAST, false, Variant.variant().with(MODEL, offModel).with(X_ROT, R90).with(Y_ROT, R90)
                                                )
                                                .select(
                                                        WEST, false, Variant.variant().with(MODEL, offModel).with(X_ROT, R90).with(Y_ROT, R270)
                                                )
                                                .select(
                                                        DOWN, true, Variant.variant().with(MODEL, onModel).with(X_ROT, R180)
                                                )
                                                .select(
                                                        UP, true, Variant.variant().with(MODEL, onModel)
                                                )
                                                .select(
                                                        NORTH, true, Variant.variant().with(MODEL, onModel).with(X_ROT, R90)
                                                )
                                                .select(
                                                        SOUTH, true, Variant.variant().with(MODEL, onModel).with(X_ROT, R90).with(Y_ROT, R180)
                                                )
                                                .select(
                                                        EAST, true, Variant.variant().with(MODEL, onModel).with(X_ROT, R90).with(Y_ROT, R90)
                                                )
                                                .select(
                                                        WEST, true, Variant.variant().with(MODEL, onModel).with(X_ROT, R90).with(Y_ROT, R270)
                                                )
                                )
                );
    }

    public void createElectromagneticDustBox(BlockModelGenerators BMG, Block block) {
        TextureMapping offMapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "_bottom"))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top"))
                .put(SIDE, TextureMapping.getBlockTexture(block, "_side"));
        ResourceLocation offModel = ModelTemplates.CUBE_BOTTOM_TOP.create(block, offMapping, BMG.modelOutput);

        TextureMapping onMapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(block, "_bottom"))
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_top_on"))
                .put(SIDE, TextureMapping.getBlockTexture(block, "_side"));
        ResourceLocation onModel = ModelTemplates.CUBE_BOTTOM_TOP.createWithOverride(block, "_on", onMapping, BMG.modelOutput);

        BMG.blockStateOutput
                .accept(
                        MultiVariantGenerator.multiVariant(block)
                                .with(
                                        PropertyDispatch.properties(BlockStateProperties.FACING, BlockStateProperties.POWERED)
                                                .select(
                                                        DOWN, false, Variant.variant().with(MODEL, offModel).with(X_ROT, R180)
                                                )
                                                .select(
                                                        UP, false, Variant.variant().with(MODEL, offModel)
                                                )
                                                .select(
                                                        NORTH, false, Variant.variant().with(MODEL, offModel).with(X_ROT, R90)
                                                )
                                                .select(
                                                        SOUTH, false, Variant.variant().with(MODEL, offModel).with(X_ROT, R90).with(Y_ROT, R180)
                                                )
                                                .select(
                                                        EAST, false, Variant.variant().with(MODEL, offModel).with(X_ROT, R90).with(Y_ROT, R90)
                                                )
                                                .select(
                                                        WEST, false, Variant.variant().with(MODEL, offModel).with(X_ROT, R90).with(Y_ROT, R270)
                                                )
                                                .select(
                                                        DOWN, true, Variant.variant().with(MODEL, onModel).with(X_ROT, R180)
                                                )
                                                .select(
                                                        UP, true, Variant.variant().with(MODEL, onModel)
                                                )
                                                .select(
                                                        NORTH, true, Variant.variant().with(MODEL, onModel).with(X_ROT, R90)
                                                )
                                                .select(
                                                        SOUTH, true, Variant.variant().with(MODEL, onModel).with(X_ROT, R90).with(Y_ROT, R180)
                                                )
                                                .select(
                                                        EAST, true, Variant.variant().with(MODEL, onModel).with(X_ROT, R90).with(Y_ROT, R90)
                                                )
                                                .select(
                                                        WEST, true, Variant.variant().with(MODEL, onModel).with(X_ROT, R90).with(Y_ROT, R270)
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
                .put(SIDE, TextureMapping.getBlockTexture(block, "_side"))
                .put(HyphaPiraceaModelTemplates.TINT_SIDE, TextureMapping.getBlockTexture(block, "_side_tint"));
        ResourceLocation verticalModel = HyphaPiraceaModelTemplates.CUBE_BOTTOM_TOP_TINTED_SIDES.create(block, verticalMapping, BMG.modelOutput);

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

    public void createHyphalNode(BlockModelGenerators BMG, Block block) {
        TextureMapping indicatorMapping = new TextureMapping()
                .put(PARTICLE, TextureMapping.getBlockTexture(block, "_open_front"))
                .put(SIDE, TextureMapping.getBlockTexture(block, "_open_side"))
                .put(BACK, TextureMapping.getBlockTexture(block, "_open_back"));

        ResourceLocation resourceLocation = ModelTemplates.SINGLE_FACE.createWithSuffix(block, "_open", TextureMapping.defaultTexture(TextureMapping.getBlockTexture(block, "_open_front")), BMG.modelOutput);
        ResourceLocation resourceLocation2 = ModelTemplates.SINGLE_FACE.create(block, TextureMapping.defaultTexture(block), BMG.modelOutput);
        ResourceLocation resourceLocation3 = HyphaPiraceaModelTemplates.SINGLE_FACE_WITH_INDICATORS.createWithSuffix(block, "_open_indicator", indicatorMapping, BMG.modelOutput);
        BMG.blockStateOutput
                .accept(
                        MultiPartGenerator.multiPart(block)
                                .with(
                                        Condition.condition().term(BlockStateProperties.NORTH, false),
                                        Variant.variant().with(
                                                VariantProperties.MODEL, resourceLocation2)
                                )
                                .with(
                                        Condition.condition().term(BlockStateProperties.EAST, false),
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, resourceLocation2)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                                                .with(VariantProperties.UV_LOCK, false)
                                )
                                .with(
                                        Condition.condition().term(BlockStateProperties.SOUTH, false),
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, resourceLocation2)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.UV_LOCK, false)
                                )
                                .with(
                                        Condition.condition().term(BlockStateProperties.WEST, false),
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, resourceLocation2)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                                                .with(VariantProperties.UV_LOCK, false)
                                )
                                .with(
                                        Condition.condition().term(BlockStateProperties.UP, false),
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, resourceLocation2)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                                                .with(VariantProperties.UV_LOCK, false)
                                )
                                .with(
                                        Condition.condition().term(BlockStateProperties.DOWN, false),
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, resourceLocation2)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                                .with(VariantProperties.UV_LOCK, false)
                                )
                                .with(
                                        Condition.condition().term(BlockStateProperties.NORTH, true),
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, resourceLocation)
                                )
                                .with(
                                        Condition.condition().term(BlockStateProperties.EAST, true),
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, resourceLocation)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                                                .with(VariantProperties.UV_LOCK, false)
                                )
                                .with(
                                        Condition.condition().term(BlockStateProperties.SOUTH, true),
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, resourceLocation)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.UV_LOCK, false)
                                )
                                .with(
                                        Condition.condition().term(BlockStateProperties.WEST, true),
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, resourceLocation)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                                                .with(VariantProperties.UV_LOCK, false)
                                )
                                .with(
                                        Condition.condition().term(BlockStateProperties.UP, true),
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, resourceLocation)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                                                .with(VariantProperties.UV_LOCK, false)
                                )
                                .with(
                                        Condition.condition().term(BlockStateProperties.DOWN, true),
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, resourceLocation)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                                .with(VariantProperties.UV_LOCK, false)
                                )
                                .with(
                                        Condition.condition().term(BlockStateProperties.NORTH, true),
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, resourceLocation3)
                                )
                                .with(
                                        Condition.condition().term(BlockStateProperties.EAST, true),
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, resourceLocation3)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)
                                                .with(VariantProperties.UV_LOCK, false)
                                )
                                .with(
                                        Condition.condition().term(BlockStateProperties.SOUTH, true),
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, resourceLocation3)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                                                .with(VariantProperties.UV_LOCK, false)
                                )
                                .with(
                                        Condition.condition().term(BlockStateProperties.WEST, true),
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, resourceLocation3)
                                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)
                                                .with(VariantProperties.UV_LOCK, false)
                                )
                                .with(
                                        Condition.condition().term(BlockStateProperties.UP, true),
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, resourceLocation3)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270)
                                                .with(VariantProperties.UV_LOCK, false)
                                )
                                .with(
                                        Condition.condition().term(BlockStateProperties.DOWN, true),
                                        Variant.variant()
                                                .with(VariantProperties.MODEL, resourceLocation3)
                                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90)
                                                .with(VariantProperties.UV_LOCK, false)
                                )
                );
        BMG.delegateItemModel(block, TexturedModel.CUBE.createWithSuffix(block, "_inventory", BMG.modelOutput));
    }

    public void createLeukbox(BlockModelGenerators BMG, Block block) {
        TextureMapping offMapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_front"))
                .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(block, "_bottom"))
                .put(TextureSlot.UP, TextureMapping.getBlockTexture(block, "_top"))
                .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(block, "_front"))
                .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(block, "_back"))
                .put(TextureSlot.EAST, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.WEST, TextureMapping.getBlockTexture(block, "_side"));
        ResourceLocation emptyModel = ModelTemplates.CUBE.create(block, offMapping, BMG.modelOutput);

        TextureMapping onMapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_front_on"))
                .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(block, "_bottom"))
                .put(TextureSlot.UP, TextureMapping.getBlockTexture(block, "_top_on"))
                .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(block, "_front_on"))
                .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(block, "_back"))
                .put(TextureSlot.EAST, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.WEST, TextureMapping.getBlockTexture(block, "_side"));
        ResourceLocation fullModel = ModelTemplates.CUBE.createWithSuffix(block, "_on", onMapping, BMG.modelOutput);

        BMG.blockStateOutput
                .accept(
                        MultiVariantGenerator.multiVariant(block)
                                .with(BlockModelGenerators.createBooleanModelDispatch(LeukboxBlock.HAS_DISC, fullModel, emptyModel))
                                .with(createHorizontalFacingDispatch())
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
