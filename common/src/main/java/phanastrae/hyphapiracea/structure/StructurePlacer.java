package phanastrae.hyphapiracea.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import phanastrae.hyphapiracea.block.HyphaPiraceaBlockTags;
import phanastrae.hyphapiracea.block.HyphaPiraceaBlocks;
import phanastrae.hyphapiracea.particle.HyphaPiraceaParticleTypes;
import phanastrae.hyphapiracea.util.IntNoise2D;

public class StructurePlacer {
    public static final IntNoise2D NOISE = generateNoise();
    public static final int MAX_NOISE_DELAY = 5;
    public static final int CONVERSION_DELAY = 50;
    public static final int SPAWN_TIME_PADDING = 8;

    public static boolean isStateFragile(BlockState state, LevelReader levelReader, BlockPos blockPos) {
        Block block = state.getBlock();
        if(block instanceof EntityBlock) {
            return true;
        }

        if(state.is(HyphaPiraceaBlockTags.PLACEMENT_FRAGILE)) {
            return true;
        }

        if(block instanceof DoorBlock) {
            // bottoms of doors are not always properly detected by the canSurvive check
            return true;
        }

        if(!state.canSurvive(levelReader, blockPos)) {
            // this prevents some, but not all, problems
            return true;
        }

        return false;
    }

    public static boolean isStateFeastable(BlockState state, BlockGetter level, BlockPos pos) {
        Block block = state.getBlock();
        if(block instanceof EntityBlock) {
            // cannot consume block entities
            return false;
        }

        if(state.is(HyphaPiraceaBlockTags.NOT_FEASTABLE)) {
            return false;
        }

        float destroySpeed = state.getDestroySpeed(level, pos);
        if(destroySpeed == -1.0F) {
            // cannot feast upon indestructible blocks, ie bedrock
            return false;
        }

        return true;
    }

    public static boolean isStateSubsumed(BlockState state) {
        return state.is(HyphaPiraceaBlocks.PIRACEATIC_TAR);
    }

    public static boolean canOperateUnderFields(Vec3 leukboxField, Vec3 localField, float minOperatingTesla) {
        // require that minOperatingTesla < localFieldStrength < leukboxFieldStrength
        double leukboxFieldStrengthSqr = leukboxField.lengthSqr();
        double localFieldStrengthSqr = localField.lengthSqr();
        if(leukboxFieldStrengthSqr < localFieldStrengthSqr) {
            return false;
        }
        if(localFieldStrengthSqr < minOperatingTesla * minOperatingTesla) {
            return false;
        }

        // dot(locF, leuF) = |loc| * |leu| * cos(theta)
        // => |dot(loc(f), leu(f)| = |loc| * |leu| * |cos(theta)| >= |leu|^2 * |cos(theta)| >= mOT^2 * |cos(theta)|
        // so requiring dot < -mOT^2 requires either
        // a) cos(theta) = 1 (directly aligned) or
        // b) 0 < cos(theta) < 1, and the product of the field strengths is sufficiently greater than mOT
        double dot = localField.dot(leukboxField);
        return dot < -minOperatingTesla * minOperatingTesla;
    }

    public static void setBlock(ServerLevel level, BlockPos pos, BlockState state, boolean updateNeighbors) {
        level.setBlock(pos, state, updateNeighbors ? 3 : 2, 512);
    }

    public static void tryUpdateSelf(ServerLevel level, BlockPos pos, BlockState state) {
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            fluidState.tick(level, pos);
        }

        Block block = state.getBlock();
        if (!(block instanceof LiquidBlock)) {
            BlockState newState = Block.updateFromNeighbourShapes(state, level, pos);
            if (!newState.equals(state)) {
                level.setBlock(pos, newState, 20);
            }
        }
    }

    public static void spawnConsumeParticles(ServerLevel level, BlockPos pos, BlockState oldState, float probability) {
        RandomSource random = level.getRandom();
        if(random.nextFloat() < probability) {
            float f = random.nextFloat();
            if(f < 0.7) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, oldState), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.9, 0.9, 0.9, 0.1);
            } else if(f < 0.9) {
                level.sendParticles(HyphaPiraceaParticleTypes.FAIRY_FOG, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 9, 1.2, 1.2, 1.2, 1);
            } else {
                level.sendParticles(HyphaPiraceaParticleTypes.ELECTROMAGNETIC_DUST, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.9, 0.9, 0.9, 1);
            }
        }
    }

    public static void spawnDissolveParticles(ServerLevel level, BlockPos pos, float probability) {
        RandomSource random = level.getRandom();
        if(random.nextFloat() < probability) {
            float f = random.nextFloat();
            if(f < 0.6) {
                level.sendParticles(HyphaPiraceaParticleTypes.PIRACITE_BUBBLE_POP, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.9, 0.9, 0.9, 0.1);
            } else if(f < 0.9) {
                level.sendParticles(HyphaPiraceaParticleTypes.FAIRY_FOG, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 9, 1.2, 1.2, 1.2, 1);
            } else {
                level.sendParticles(HyphaPiraceaParticleTypes.ELECTROMAGNETIC_DUST, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.9, 0.9, 0.9, 1);
            }
        }
    }

    public static boolean checkLoadedOrOutOfRange(ServerLevel level, BoundingBox box, BlockPos leukboxPos, float maxOperatingRadius) {
        return SectionPos.betweenClosedStream(
                SectionPos.blockToSectionCoord(box.minX()),
                SectionPos.blockToSectionCoord(box.minY()),
                SectionPos.blockToSectionCoord(box.minZ()),
                SectionPos.blockToSectionCoord(box.maxX()),
                SectionPos.blockToSectionCoord(box.maxY()),
                SectionPos.blockToSectionCoord(box.maxZ())
        ).allMatch(sectionPos -> {
            if(!isChunkSectionInRange(sectionPos, leukboxPos, maxOperatingRadius)) {
                return true;
            } else {
                return level.isLoaded(sectionPos.chunk().getWorldPosition());
            }
        });
    }

    public static boolean isChunkSectionInRange(SectionPos sectionPos, BlockPos leukboxPos, float radius) {
        Vec3 sectionCore = sectionPos.center().getCenter();
        Vec3 leukboxCore = leukboxPos.getCenter();
        double dist = sectionCore.distanceTo(leukboxCore);
        double centerToCornerDist = 8 * 1.7321;

        return dist < radius + centerToCornerDist;
    }

    public static boolean isPositionInRange(BlockPos pos, BlockPos leukboxPos, double radius) {
        return pos.distSqr(leukboxPos) < radius * radius;
    }

    public static boolean isPositionInRange(Vec3 pos, BlockPos leukboxPos, double radius) {
        return pos.distanceToSqr(leukboxPos.getCenter()) < radius * radius;
    }

    public static boolean tInRange(double t, double time) {
        return time - 1 < t && t <= time;
    }

    public static double calcHorizontalFactor(double dx, double dz) {
        return Math.sqrt(dx*dx+dz*dz);
    }

    public static boolean isBoxInTimeRange(BoundingBox box, BlockPos pos, int currentSpawnTime) {
        int[] times = calcSpawnTimes(box, pos);
        int min = times[0];
        int max = times[1];

        if(currentSpawnTime - 1 >= max || min > currentSpawnTime) {
            return false;
        } else {
            return true;
        }
    }

    public static int[] calcSpawnTimesForSphere(Vec3 position, float radius, BlockPos core) {
        Vec3 corePos = core.getCenter();
        Vec3 offset = position.subtract(corePos);

        double maxDy = offset.y + radius;
        double minDy = offset.y - radius;

        // spawn times are linear in r, -dy, n so just input the appropriate min/max values of each parameter to min/maximise the time
        // given this is a sphere the r and dy are linked and so by inputting the extremes this may overestimate the min/maximum, but this is probably fine
        // time is min at closest (h=0), highest (dy=maxDy) point
        double minTime = calcSpawnTime(0, maxDy, 0);
        int min = Mth.floor(minTime) - SPAWN_TIME_PADDING;
        // time is low at furthest (h=maxH), lowest (dy=minDy) point
        double maxTime = calcSpawnTime(radius, minDy, MAX_NOISE_DELAY - 1);
        int max = Mth.ceil(maxTime) + CONVERSION_DELAY + SPAWN_TIME_PADDING;

        return new int[]{min, max};
    }

    public static int[] calcSpawnTimes(BoundingBox box, BlockPos core) {
        // returns an int array of min, max
        int dMinX = box.minX() - core.getX();
        int dMaxX = box.maxX() - core.getX();
        int dMinZ = box.minZ() - core.getZ();
        int dMaxZ = box.maxZ() - core.getZ();

        // horizontal distance is maximised at one of the corners, so just check those
        double maxH = 0;
        for(int i = 0; i < 4; i++) {
            int x = (i & 0x1) == 0 ? dMinX : dMaxX;
            int z = (i & 0x2) == 0 ? dMinZ : dMaxZ;

            double h = calcHorizontalFactor(x, z);
            if(h > maxH) {
                maxH = h;
            }
        }

        int minDy = box.minY() - core.getY();
        int maxDy = box.maxY() - core.getY();

        // spawn times are linear in r, -dy, n so just input the appropriate min/max values of each parameter to min/maximise the time
        // time is min at closest (h=0), highest (dy=maxDy) point
        double minTime = calcSpawnTime(0, maxDy, 0);
        int min = Mth.floor(minTime) - SPAWN_TIME_PADDING;
        // time is low at furthest (h=maxH), lowest (dy=minDy) point
        double maxTime = calcSpawnTime(maxH, minDy, MAX_NOISE_DELAY - 1);
        int max = Mth.ceil(maxTime) + CONVERSION_DELAY + SPAWN_TIME_PADDING;

        return new int[]{min, max};
    }

    public static double calcSpawnTime(double h, double dy, double n) {
        return h * 7.0F + 0.9F * -dy + n * 4.5F;
    }

    public static IntNoise2D generateNoise() {
        RandomSource randomSource = RandomSource.create(12345);
        return IntNoise2D.generateNoise(128, 128, () -> randomSource.nextInt(MAX_NOISE_DELAY));
    }
}
