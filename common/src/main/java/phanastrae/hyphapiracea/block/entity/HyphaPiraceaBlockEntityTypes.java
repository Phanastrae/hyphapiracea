package phanastrae.hyphapiracea.block.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import phanastrae.hyphapiracea.HyphaPiracea;
import phanastrae.hyphapiracea.block.HyphaPiraceaBlocks;

import java.util.function.BiConsumer;

public class HyphaPiraceaBlockEntityTypes {

    public static final BlockEntityType<LeukboxBlockEntity> PIRACEATIC_LEUKBOX = create("leukbox", LeukboxBlockEntity::new, HyphaPiraceaBlocks.PIRACEATIC_LEUKBOX);
    public static final BlockEntityType<HyphalConductorBlockEntity> HYPHAL_CONDUCTOR = create("hyphal_conductor", HyphalConductorBlockEntity::new, HyphaPiraceaBlocks.HYPHAL_CONDUCTOR);
    public static final BlockEntityType<MagnetometerBlockEntity> LEYFIELD_MAGNETOMETER_BLOCK = create("magnetometer_block", MagnetometerBlockEntity::new, HyphaPiraceaBlocks.LEYFIELD_MAGNETOMETER_BLOCK);
    public static final BlockEntityType<HyphalNodeBlockEntity> HYPHAL_NODE = create("galvanocarpic_bulb", HyphalNodeBlockEntity::new, HyphaPiraceaBlocks.HYPHAL_NODE);
    public static final BlockEntityType<AmmeterBlockEntity> HYPHAL_AMMETER = create("ammeter_block", AmmeterBlockEntity::new, HyphaPiraceaBlocks.HYPHAL_AMMETER);
    public static final BlockEntityType<VoltmeterBlockEntity> HYPHAL_VOLTMETER = create("voltmeter_block", VoltmeterBlockEntity::new, HyphaPiraceaBlocks.HYPHAL_VOLTMETER);
    public static final BlockEntityType<StormsapCellBlockEntity> STORMSAP_CELL = create("stormsap_cell", StormsapCellBlockEntity::new, HyphaPiraceaBlocks.STORMSAP_CELL);
    public static final BlockEntityType<CreativeCellBlockEntity> CREATIVE_CELL = create("creative_cell", CreativeCellBlockEntity::new, HyphaPiraceaBlocks.CREATIVE_CELL);
    public static final BlockEntityType<CircuitSwitchBlockEntity> CIRCUIT_SWITCH = create("circuit_switch", CircuitSwitchBlockEntity::new, HyphaPiraceaBlocks.CIRCUIT_SWITCH);

    public static void init(BiConsumer<ResourceLocation, BlockEntityType<?>> r) {
        r.accept(id("piraceatic_leukbox"), PIRACEATIC_LEUKBOX);
        r.accept(id("hyphal_conductor"), HYPHAL_CONDUCTOR);
        r.accept(id("magnetometer_block"), LEYFIELD_MAGNETOMETER_BLOCK);
        r.accept(id("hyphal_node"), HYPHAL_NODE);
        r.accept(id("ammeter_block"), HYPHAL_AMMETER);
        r.accept(id("voltmeter_block"), HYPHAL_VOLTMETER);
        r.accept(id("stormsap_cell"), STORMSAP_CELL);
        r.accept(id("creative_cell"), CREATIVE_CELL);
        r.accept(id("circuit_switch"), CIRCUIT_SWITCH);
    }

    private static ResourceLocation id(String path) {
        return HyphaPiracea.id(path);
    }

    private static <T extends BlockEntity> BlockEntityType<T> create(String id, BlockEntityType.BlockEntitySupplier<? extends T> factory, Block... blocks) {
        if (blocks.length == 0) {
            HyphaPiracea.LOGGER.warn("Block entity type {} requires at least one valid block to be defined!", id);
        }
        return BlockEntityType.Builder.<T>of(factory, blocks).build(null);
    }
}
