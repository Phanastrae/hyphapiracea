package phanastrae.hyphapiracea.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import phanastrae.hyphapiracea.HyphaPiracea;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class HyphaPiraceaBlocks {

    public static final Block AZIMULDEY_MASS = new AzimuldeyMassBlock(properties()
            .mapColor(MapColor.COLOR_GREEN)
            .instrument(NoteBlockInstrument.BASS)
            .strength(1.2F)
            .sound(SoundType.NYLIUM)
    );

    public static final Block AZIMULIC_STEM = new AzimulicStemBlock(properties()
            .mapColor(MapColor.COLOR_GREEN)
            .instrument(NoteBlockInstrument.BASS)
            .strength(1.2F)
            .sound(SoundType.NYLIUM)
    );

    public static final Block HYPHAL_NODE = new HyphalNodeBlock(properties()
            .mapColor(MapColor.COLOR_GREEN)
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.0F)
            .sound(SoundType.NYLIUM)
    );

    public static final Block HYPHAL_STEM = new HyphalStemBlock(properties()
            .mapColor(MapColor.COLOR_GREEN)
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.0F)
            .sound(SoundType.NYLIUM)
    );

    public static final Block HYPHAL_CONDUCTOR = new HyphalConductorBlock(properties()
            .mapColor(MapColor.COLOR_GREEN)
            .strength(1.8F)
            .instrument(NoteBlockInstrument.BASS)
            .sound(SoundType.NYLIUM)
    );

    public static final Block OAK_CONDUCTOR = createWoodConductor(Blocks.OAK_LOG);
    public static final Block SPRUCE_CONDUCTOR = createWoodConductor(Blocks.SPRUCE_LOG);
    public static final Block BIRCH_CONDUCTOR = createWoodConductor(Blocks.BIRCH_LOG);
    public static final Block JUNGLE_CONDUCTOR = createWoodConductor(Blocks.JUNGLE_LOG);
    public static final Block ACACIA_CONDUCTOR = createWoodConductor(Blocks.ACACIA_LOG);
    public static final Block DARK_OAK_CONDUCTOR = createWoodConductor(Blocks.DARK_OAK_LOG);
    public static final Block MANGROVE_CONDUCTOR = createWoodConductor(Blocks.MANGROVE_LOG);
    public static final Block CHERRY_CONDUCTOR = createWoodConductor(Blocks.CHERRY_LOG);
    public static final Block BAMBOO_CONDUCTOR = createWoodConductor(Blocks.BAMBOO_BLOCK);
    public static final Block CRIMSON_CONDUCTOR = createNetherWoodConductor(Blocks.CRIMSON_STEM);
    public static final Block WARPED_CONDUCTOR = createNetherWoodConductor(Blocks.WARPED_STEM);

    public static final List<Block> CONDUCTORS = getConductorList();

    public static List<Block> getConductorList() {
        List<Block> c = new ArrayList<>();
        c.add(HYPHAL_CONDUCTOR);
        c.add(OAK_CONDUCTOR);
        c.add(SPRUCE_CONDUCTOR);
        c.add(BIRCH_CONDUCTOR);
        c.add(JUNGLE_CONDUCTOR);
        c.add(ACACIA_CONDUCTOR);
        c.add(DARK_OAK_CONDUCTOR);
        c.add(MANGROVE_CONDUCTOR);
        c.add(CHERRY_CONDUCTOR);
        c.add(BAMBOO_CONDUCTOR);
        c.add(CRIMSON_CONDUCTOR);
        c.add(WARPED_CONDUCTOR);
        return c;
    }

    public static final Block STORMSAP_CELL = new StormsapCellBlock(properties()
            .mapColor(MapColor.COLOR_LIGHT_BLUE)
            .instrument(NoteBlockInstrument.BASS)
            .strength(3.0F)
            .sound(SoundType.COPPER_BULB)
            .lightLevel(blockState -> blockState.getValue(StormsapCellBlock.STORED_POWER))
    );

    public static final Block CREATIVE_CELL = new CreativeCellBlock(properties()
            .mapColor(MapColor.COLOR_MAGENTA)
            .instrument(NoteBlockInstrument.BASS)
            .strength(5.0F)
            .sound(SoundType.COPPER_BULB)
            .lightLevel(blockState -> 12)
    );

    public static final Block HYPHAL_AMMETER = new AmmeterBlock(properties()
            .mapColor(MapColor.COLOR_YELLOW)
            .instrument(NoteBlockInstrument.BASS)
            .strength(3.0F)
            .sound(SoundType.COPPER_BULB)
    );

    public static final Block HYPHAL_VOLTMETER = new VoltmeterBlock(properties()
            .mapColor(MapColor.COLOR_PURPLE)
            .instrument(NoteBlockInstrument.BASS)
            .strength(3.0F)
            .sound(SoundType.COPPER_BULB)
    );

    public static final Block CIRCUIT_SWITCH = new CircuitSwitchBlock(properties()
            .mapColor(MapColor.COLOR_RED)
            .instrument(NoteBlockInstrument.BASS)
            .strength(3.0F)
            .sound(SoundType.COPPER_BULB)
            .lightLevel(blockState -> blockState.getValue(CircuitSwitchBlock.POWERED) ? 4 : 0)
    );

    public static final Block LEYFIELD_MAGNETOMETER_BLOCK = new MagnetometerBlock(properties()
            .mapColor(MapColor.COLOR_LIGHT_GRAY)
            .instrument(NoteBlockInstrument.BASS)
            .strength(3.5F)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
    );

    public static final Block ELECTROMAGNETIC_DUST_BOX = new ElectromagneticDustBoxBlock(properties()
            .mapColor(MapColor.COLOR_LIGHT_BLUE)
            .instrument(NoteBlockInstrument.BASS)
            .strength(2.4F)
            .sound(SoundType.COPPER_BULB)
    );

    public static final Block PIRACEATIC_LEUKBOX = new LeukboxBlock(properties()
            .mapColor(MapColor.SAND)
            .instrument(NoteBlockInstrument.XYLOPHONE)
            .strength(3.0F, 10.0F)
            .sound(SoundType.BONE_BLOCK)
            .lightLevel(blockState -> blockState.getValue(LeukboxBlock.HAS_DISC) ? 11 : 2)
    );

    public static final Block PIRACEATIC_TAR = new PiraceaticTarBlock(properties()
            .mapColor(MapColor.COLOR_RED)
            .instrument(NoteBlockInstrument.CHIME)
            .strength(0.8F)
            .sound(SoundType.SLIME_BLOCK)
            .emissiveRendering((state, blockGetter, blockPos) -> true)
            .lightLevel((blockState) -> 13)
            .isValidSpawn(HyphaPiraceaBlocks::never)
            .isRedstoneConductor(HyphaPiraceaBlocks::never)
            .isSuffocating(HyphaPiraceaBlocks::never)
            .isViewBlocking(HyphaPiraceaBlocks::never)
            .noOcclusion()
            .randomTicks()
    );

    public static final Block PIRACEATIC_GLOBGLASS = new PiraceaticGlobglassBlock(properties()
            .mapColor(MapColor.COLOR_RED)
            .instrument(NoteBlockInstrument.CHIME)
            .strength(0.9F)
            .sound(SoundType.SLIME_BLOCK)
            .emissiveRendering((state, blockGetter, blockPos) -> true)
            .lightLevel((blockState) -> 13)
            .isValidSpawn(HyphaPiraceaBlocks::never)
            .isRedstoneConductor(HyphaPiraceaBlocks::never)
            .isSuffocating(HyphaPiraceaBlocks::never)
            .isViewBlocking(HyphaPiraceaBlocks::never)
            .noOcclusion()
    );

    public static void init(BiConsumer<ResourceLocation, Block> r) {
        r.accept(id("azimuldey_mass"), AZIMULDEY_MASS);
        r.accept(id("azimulic_stem"), AZIMULIC_STEM);

        r.accept(id("hyphal_node"), HYPHAL_NODE);
        r.accept(id("hyphal_stem"), HYPHAL_STEM);

        r.accept(id("hyphal_conductor"), HYPHAL_CONDUCTOR);
        r.accept(id("oak_conductor"), OAK_CONDUCTOR);
        r.accept(id("spruce_conductor"), SPRUCE_CONDUCTOR);
        r.accept(id("birch_conductor"), BIRCH_CONDUCTOR);
        r.accept(id("jungle_conductor"), JUNGLE_CONDUCTOR);
        r.accept(id("acacia_conductor"), ACACIA_CONDUCTOR);
        r.accept(id("dark_oak_conductor"), DARK_OAK_CONDUCTOR);
        r.accept(id("mangrove_conductor"), MANGROVE_CONDUCTOR);
        r.accept(id("cherry_conductor"), CHERRY_CONDUCTOR);
        r.accept(id("bamboo_conductor"), BAMBOO_CONDUCTOR);
        r.accept(id("crimson_conductor"), CRIMSON_CONDUCTOR);
        r.accept(id("warped_conductor"), WARPED_CONDUCTOR);

        r.accept(id("stormsap_cell"), STORMSAP_CELL);
        r.accept(id("creative_cell"), CREATIVE_CELL);
        r.accept(id("ammeter_block"), HYPHAL_AMMETER);
        r.accept(id("voltmeter_block"), HYPHAL_VOLTMETER);
        r.accept(id("circuit_switch"), CIRCUIT_SWITCH);

        r.accept(id("magnetometer_block"), LEYFIELD_MAGNETOMETER_BLOCK);

        r.accept(id("electromagnetic_dust_box"), ELECTROMAGNETIC_DUST_BOX);

        r.accept(id("piraceatic_leukbox"), PIRACEATIC_LEUKBOX);
        r.accept(id("piraceatic_tar"), PIRACEATIC_TAR);
        r.accept(id("piraceatic_globglass"), PIRACEATIC_GLOBGLASS);
    }

    private static Block createWoodConductor(Block base) {
        return new HyphalConductorBlock(properties()
                .mapColor(base.defaultMapColor())
                .strength(1.8F)
                .instrument(NoteBlockInstrument.BASS)
                .sound(SoundType.WOOD)
                .ignitedByLava()
        );
    }

    private static Block createNetherWoodConductor(Block base) {
        return new HyphalConductorBlock(properties()
                .mapColor(base.defaultMapColor())
                .strength(1.8F)
                .instrument(NoteBlockInstrument.BASS)
                .sound(SoundType.NETHER_WOOD)
        );
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
