package phanastrae.hyphapiracea.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;
import phanastrae.hyphapiracea.block.HyphaPiraceaBlocks;
import phanastrae.hyphapiracea.item.HyphaPiraceaItems;

import java.util.concurrent.CompletableFuture;

public class BlockLootTableProvider extends FabricBlockLootTableProvider {
    protected BlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        dropSelf(HyphaPiraceaBlocks.AZIMULDEY_MASS);
        dropSelf(HyphaPiraceaBlocks.AZIMULIC_STEM);
        dropSelf(HyphaPiraceaBlocks.HYPHAL_NODE);
        dropSelf(HyphaPiraceaBlocks.HYPHAL_STEM);

        for(Block conductor : HyphaPiraceaBlocks.CONDUCTORS) {
            dropSelf(conductor);
        }

        dropSelf(HyphaPiraceaBlocks.STORMSAP_CELL);
        dropSelf(HyphaPiraceaBlocks.CREATIVE_CELL);
        dropSelf(HyphaPiraceaBlocks.HYPHAL_AMMETER);
        dropSelf(HyphaPiraceaBlocks.HYPHAL_VOLTMETER);
        dropSelf(HyphaPiraceaBlocks.CIRCUIT_SWITCH);
        dropSelf(HyphaPiraceaBlocks.LEYFIELD_MAGNETOMETER_BLOCK);
        dropSelf(HyphaPiraceaBlocks.ELECTROMAGNETIC_DUST_BOX);
        dropSelf(HyphaPiraceaBlocks.PIRACEATIC_LEUKBOX);
        dropSelf(HyphaPiraceaBlocks.PIRACEATIC_GLOBGLASS);

        dropOther(HyphaPiraceaBlocks.PIRACEATIC_TAR, HyphaPiraceaItems.PIRACEATIC_GLOB);
    }
}
