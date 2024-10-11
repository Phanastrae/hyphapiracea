package phanastrae.ywsanf.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class IntermediateGenLevel implements WorldGenLevel {

    private final WorldGenLevel level;
    private final IntermediateStructureStorage intermediateStorage;

    public IntermediateGenLevel(WorldGenLevel level) {
        this.level = level;
        this.intermediateStorage = new IntermediateStructureStorage();
    }

    public IntermediateStructureStorage getStorage() {
        return this.intermediateStorage;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        BlockState state = this.intermediateStorage.getBlockState(pos);
        if(state.is(Blocks.STRUCTURE_VOID)) {
            return level.getBlockState(pos);
        } else {
            return state;
        }
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        BlockEntity blockEntity = this.intermediateStorage.getBlockEntity(pos);
        if(blockEntity != null) {
            return blockEntity;
        } else {
            BlockState state = getBlockState(pos);
            BlockState oldState = this.level.getBlockState(pos);
            if(state.equals(oldState)) {
                return level.getBlockEntity(pos);
            } else {
                return null;
            }
        }
    }

    @Override
    public boolean isStateAtPosition(BlockPos pos, Predicate<BlockState> predicate) {
        return predicate.test(getBlockState(pos));
    }

    @Override
    public boolean isFluidAtPosition(BlockPos pos, Predicate<FluidState> predicate) {
        return predicate.test(getFluidState(pos));
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        return this.intermediateStorage.setBlockState(pos, state);
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean isMoving) {
        return this.intermediateStorage.setBlockState(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock, @Nullable Entity entity, int recursionLeft) {
        return this.intermediateStorage.setBlockState(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public boolean addFreshEntity(Entity entity) {
        this.intermediateStorage.addEntity(entity);
        return true;
    }

    // everything below this point just points directly to level's version of the function

    @Override
    public long getSeed() {
        return level.getSeed();
    }

    @Override
    public ServerLevel getLevel() {
        return level.getLevel();
    }

    @Override
    public long nextSubTickCount() {
        return level.nextSubTickCount();
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return level.getBlockTicks();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return level.getFluidTicks();
    }

    @Override
    public LevelData getLevelData() {
        return level.getLevelData();
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos pos) {
        return level.getCurrentDifficultyAt(pos);
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return level.getServer();
    }

    @Override
    public ChunkSource getChunkSource() {
        return level.getChunkSource();
    }

    @Override
    public RandomSource getRandom() {
        return level.getRandom();
    }

    @Override
    public void playSound(@Nullable Player player, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        level.playSound(player, pos, sound, source, volume, pitch);
    }

    @Override
    public void addParticle(ParticleOptions particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        level.addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public void levelEvent(@Nullable Player player, int type, BlockPos pos, int data) {
        level.levelEvent(player, type, pos, data);
    }

    @Override
    public void gameEvent(Holder<GameEvent> gameEvent, Vec3 pos, GameEvent.Context context) {
        level.gameEvent(gameEvent, pos, context);
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        return level.getShade(direction, shade);
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return level.getLightEngine();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return level.getWorldBorder();
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity entity, AABB area, Predicate<? super Entity> predicate) {
        return level.getEntities(entity, area, predicate);
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> entityTypeTest, AABB bounds, Predicate<? super T> predicate) {
        return level.getEntities(entityTypeTest, bounds, predicate);
    }

    @Override
    public List<? extends Player> players() {
        return level.players();
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(int x, int z, ChunkStatus chunkStatus, boolean requireChunk) {
        return level.getChunk(x, z, chunkStatus, requireChunk);
    }

    @Override
    public int getHeight(Heightmap.Types heightmapType, int x, int z) {
        return level.getHeight(heightmapType, x, z);
    }

    @Override
    public int getSkyDarken() {
        return level.getSkyDarken();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return level.getBiomeManager();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return level.getUncachedNoiseBiome(x, y, z);
    }

    @Override
    public boolean isClientSide() {
        return level.isClientSide();
    }

    @Override
    public int getSeaLevel() {
        return level.getSeaLevel();
    }

    @Override
    public DimensionType dimensionType() {
        return level.dimensionType();
    }

    @Override
    public RegistryAccess registryAccess() {
        return level.registryAccess();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return level.enabledFeatures();
    }
}
