package phanastrae.ywsanf.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.world.item.Item;
import phanastrae.ywsanf.block.YWSaNFBlocks;
import phanastrae.ywsanf.item.YWSaNFItems;

public class ModelProvider extends FabricModelProvider {
    public ModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators BMG) {
        BMG.createTrivialCube(YWSaNFBlocks.LEUKBOX);
        BMG.createTrivialCube(YWSaNFBlocks.FEASTING_TAR);
    }

    @Override
    public void generateItemModels(ItemModelGenerators IMG) {
        generateFlat(IMG, YWSaNFItems.KEYED_DISC);
    }

    private static void generateFlat(ItemModelGenerators itemModelGenerator, Item item) {
        itemModelGenerator.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
    }
}
