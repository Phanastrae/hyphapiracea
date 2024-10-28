package phanastrae.ywsanf.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.core.Direction;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import phanastrae.ywsanf.block.YWSaNFBlocks;
import phanastrae.ywsanf.item.YWSaNFItems;

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
        BMG.createTrivialCube(YWSaNFBlocks.LEUKBOX);
        BMG.createTrivialCube(YWSaNFBlocks.FEASTING_TAR);
        BMG.createTrivialCube(YWSaNFBlocks.GALVANOCARPIC_BULB);

        this.createTopSideBottom(BMG, YWSaNFBlocks.MAGNETOMETER_BLOCK);

        this.createDirectionalBlock(BMG, YWSaNFBlocks.VOLTMETER_BLOCK);
        this.createDirectionalBlock(BMG, YWSaNFBlocks.AMMETER_BLOCK);
        this.createDirectionalBlock(BMG, YWSaNFBlocks.STORMSAP_CELL);

        createConductorBlock(BMG, YWSaNFBlocks.HYPHAL_CONDUCTOR);
    }

    @Override
    public void generateItemModels(ItemModelGenerators IMG) {
        generateFlat(IMG,
                YWSaNFItems.KEYED_DISC,
                YWSaNFItems.HYPHALINE
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
        ResourceLocation verticalModel = YWSaNFModelTemplates.CONDUCTOR.create(block, verticalMapping, BMG.modelOutput);

        TextureMapping horizontalMapping = new TextureMapping()
                .put(PARTICLE, getBlockTexture(Blocks.POLISHED_DEEPSLATE))
                .put(ALL, getBlockTexture(Blocks.POLISHED_DEEPSLATE));
        ResourceLocation horizontalModel = YWSaNFModelTemplates.CONDUCTOR_WALL.create(block, horizontalMapping, BMG.modelOutput);

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

    public void createDirectionalBlock(BlockModelGenerators BMG, Block block) {
        TextureMapping horizontalMapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.UP, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(block, "_front"))
                .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(block, "_back"))
                .put(TextureSlot.EAST, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.WEST, TextureMapping.getBlockTexture(block, "_side"));
        ResourceLocation horizontalModel = ModelTemplates.CUBE.create(block, horizontalMapping, BMG.modelOutput);

        TextureMapping verticalMapping = new TextureMapping()
                .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(block, "_back"))
                .put(TextureSlot.UP, TextureMapping.getBlockTexture(block, "_front"))
                .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.EAST, TextureMapping.getBlockTexture(block, "_side"))
                .put(TextureSlot.WEST, TextureMapping.getBlockTexture(block, "_side"));
        ResourceLocation verticalModel = ModelTemplates.CUBE.createWithSuffix(block, "_vertical", verticalMapping, BMG.modelOutput);

        BMG.blockStateOutput
                .accept(
                        MultiVariantGenerator.multiVariant(block)
                                .with(
                                        PropertyDispatch.property(BlockStateProperties.FACING)
                                                .select(
                                                        Direction.DOWN, Variant.variant().with(VariantProperties.MODEL, verticalModel).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180)
                                                )
                                                .select(Direction.UP, Variant.variant().with(VariantProperties.MODEL, verticalModel))
                                                .select(Direction.NORTH, Variant.variant().with(VariantProperties.MODEL, horizontalModel))
                                                .select(Direction.EAST, Variant.variant().with(VariantProperties.MODEL, horizontalModel).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                                                .select(
                                                        Direction.SOUTH, Variant.variant().with(VariantProperties.MODEL, horizontalModel).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)
                                                )
                                                .select(Direction.WEST, Variant.variant().with(VariantProperties.MODEL, horizontalModel).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
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
