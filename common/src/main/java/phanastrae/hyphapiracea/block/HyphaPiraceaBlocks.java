package phanastrae.hyphapiracea.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import phanastrae.hyphapiracea.HyphaPiracea;

import java.util.function.BiConsumer;

public class HyphaPiraceaBlocks {

    public static final Block PIRACEATIC_LEUKBOX = new LeukboxBlock(properties());
    public static final Block HYPHAL_CONDUCTOR = new HyphalConductorBlock(properties());
    public static final Block PIRACEATIC_TAR = new PiraceaticTarBlock(properties()
            .emissiveRendering((state, blockGetter, blockPos) -> true)
            .lightLevel((blockState) -> 13)
            .isValidSpawn(HyphaPiraceaBlocks::never)
            .isRedstoneConductor(HyphaPiraceaBlocks::never)
            .isSuffocating(HyphaPiraceaBlocks::never)
            .isViewBlocking(HyphaPiraceaBlocks::never)
            .noOcclusion()
            .randomTicks()
    );
    public static final Block LEYFIELD_MAGNETOMETER_BLOCK = new MagnetometerBlock(properties());
    public static final Block HYPHAL_NODE = new HyphalNodeBlock(properties());
    public static final Block HYPHAL_AMMETER = new AmmeterBlock(properties());
    public static final Block HYPHAL_VOLTMETER = new VoltmeterBlock(properties());
    public static final Block STORMSAP_CELL = new StormsapCellBlock(properties()
            .lightLevel(blockState -> blockState.getValue(StormsapCellBlock.STORED_POWER)));
    public static final Block AZIMULDEY_MASS = new AzimuldeyMassBlock(properties());
    public static final Block CREATIVE_CELL = new CreativeCellBlock(properties()
            .lightLevel(blockState -> 12));

    public static void init(BiConsumer<ResourceLocation, Block> r) {
        r.accept(id("piraceatic_leukbox"), PIRACEATIC_LEUKBOX);
        r.accept(id("hyphal_conductor"), HYPHAL_CONDUCTOR);
        r.accept(id("piraceatic_tar"), PIRACEATIC_TAR);
        r.accept(id("magnetometer_block"), LEYFIELD_MAGNETOMETER_BLOCK);
        r.accept(id("hyphal_node"), HYPHAL_NODE);
        r.accept(id("ammeter_block"), HYPHAL_AMMETER);
        r.accept(id("voltmeter_block"), HYPHAL_VOLTMETER);
        r.accept(id("stormsap_cell"), STORMSAP_CELL);
        r.accept(id("azimuldey_mass"), AZIMULDEY_MASS);
        r.accept(id("creative_cell"), CREATIVE_CELL);
    }

    private static ResourceLocation id(String path) {
        return HyphaPiracea.id(path);
    }

    private static BlockBehaviour.Properties properties() {
        return BlockBehaviour.Properties.of();
    }

    private static Boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos, EntityType<?> entity) {
        return false;
    }

    private static boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos) {
        return false;
    }
}
