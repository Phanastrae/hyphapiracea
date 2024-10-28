package phanastrae.ywsanf.block.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import phanastrae.ywsanf.YWSaNF;
import phanastrae.ywsanf.block.YWSaNFBlocks;

import java.util.function.BiConsumer;

public class YWSaNFBlockEntityTypes {

    public static final BlockEntityType<LeukboxBlockEntity> LEUKBOX = create("leukbox", LeukboxBlockEntity::new, YWSaNFBlocks.LEUKBOX);
    public static final BlockEntityType<HyphalConductorBlockEntity> HYPHAL_CONDUCTOR = create("hyphal_conductor", HyphalConductorBlockEntity::new, YWSaNFBlocks.HYPHAL_CONDUCTOR);
    public static final BlockEntityType<MagnetometerBlockEntity> MAGNETOMETER_BLOCK = create("magnetometer_block", MagnetometerBlockEntity::new, YWSaNFBlocks.MAGNETOMETER_BLOCK);

    public static void init(BiConsumer<ResourceLocation, BlockEntityType<?>> r) {
        r.accept(id("leukbox"), LEUKBOX);
        r.accept(id("hyphal_conductor"), HYPHAL_CONDUCTOR);
        r.accept(id("magnetometer_block"), MAGNETOMETER_BLOCK);
    }

    private static ResourceLocation id(String path) {
        return YWSaNF.id(path);
    }

    private static <T extends BlockEntity> BlockEntityType<T> create(String id, BlockEntityType.BlockEntitySupplier<? extends T> factory, Block... blocks) {
        if (blocks.length == 0) {
            YWSaNF.LOGGER.warn("Block entity type {} requires at least one valid block to be defined!", id);
        }
        return BlockEntityType.Builder.<T>of(factory, blocks).build(null);
    }
}
