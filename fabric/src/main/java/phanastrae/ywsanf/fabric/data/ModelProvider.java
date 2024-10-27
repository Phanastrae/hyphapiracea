package phanastrae.ywsanf.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
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

        createConductorBlock(BMG, YWSaNFBlocks.HYPHAL_CONDUCTOR);
    }

    @Override
    public void generateItemModels(ItemModelGenerators IMG) {
        generateFlat(IMG,
                YWSaNFItems.KEYED_DISC,
                YWSaNFItems.HYPHALINE,
                YWSaNFItems.MAGNETOMETER
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
