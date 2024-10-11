package phanastrae.ywsanf.structure;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class IntermediateStructureStorage {

    private final HashMap<SectionPos, PalettedContainer<BlockState>> statePalettes;
    private final HashMap<BlockPos, BlockEntity> blockEntities;
    private final List<Entity> entities;

    public IntermediateStructureStorage() {
        this.statePalettes = new HashMap<>();
        this.blockEntities = new HashMap<>();
        this.entities = new ObjectArrayList<>();
    }

    public void forEachContainer(BiConsumer<? super SectionPos, ? super PalettedContainer<BlockState>> biConsumer) {
        this.statePalettes.forEach(biConsumer);
    }

    public void forEachBlockEntity(BiConsumer<? super BlockPos, ? super BlockEntity> biConsumer) {
        this.blockEntities.forEach(biConsumer);
    }

    public void forEachEntity(Consumer<? super Entity> consumer) {
        this.entities.forEach(consumer);
    }

    public PalettedContainer<BlockState> getContainer(SectionPos chunkPos) {
        if(statePalettes.containsKey(chunkPos)) {
            return statePalettes.get(chunkPos);
        } else {
            PalettedContainer<BlockState> container = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.STRUCTURE_VOID.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
            statePalettes.put(chunkPos, container);
            return container;
        }
    }

    public PalettedContainer<BlockState> getContainer(BlockPos pos) {
        SectionPos chunkPos = SectionPos.of(pos);
        return getContainer(chunkPos);
    }

    public boolean setBlockState(BlockPos pos, BlockState state) {
        getContainer(pos).set(pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF, state);
        if(state.hasBlockEntity() && state.getBlock() instanceof EntityBlock entityBlock) {
            this.blockEntities.remove(pos);
            BlockEntity blockEntity = entityBlock.newBlockEntity(pos, state);
            if(blockEntity != null) {
                this.blockEntities.put(pos, blockEntity);
            }
        }
        return true;
    }

    public BlockState getBlockState(BlockPos pos) {
        return getContainer(pos).get(pos.getX() & 0xF, pos.getY() & 0xF, pos.getZ() & 0xF);
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        if(this.blockEntities.containsKey(pos)) {
            BlockEntity entity = this.blockEntities.get(pos);
            return entity;
        } else {
            return null;
        }
    }

    public void addEntity(Entity entity) {
        this.entities.add(entity);
    }
}
