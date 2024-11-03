package phanastrae.hyphapiracea.structure.leubox_stages;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import phanastrae.hyphapiracea.block.HyphaPiraceaBlocks;
import phanastrae.hyphapiracea.structure.BoxedContainer;
import phanastrae.hyphapiracea.structure.IntermediateStructureStorage;
import phanastrae.hyphapiracea.structure.StructurePlacer;

import java.util.LinkedList;

import static phanastrae.hyphapiracea.structure.StructurePlacer.calcSpawnTimes;
import static phanastrae.hyphapiracea.structure.StructurePlacer.calcSpawnTimesForSphere;

public class PostProcessStage extends AbstractLeukboxStage {

    private final IntermediateStructureStorage intermediateStructureStorage;
    private final BoundingBox boundingBox;
    private final LinkedList<Pair<SectionPos, BoxedContainer>> unprocessedBoxes;

    public PostProcessStage(BlockPos leukboxPos, IntermediateStructureStorage intermediateStructureStorage, BoundingBox boundingBox, LinkedList<Pair<SectionPos, BoxedContainer>> unprocessedBoxes, int requiredOperations) {
        super(leukboxPos, LeukboxStage.POST_PROCESS);

        this.intermediateStructureStorage = intermediateStructureStorage;
        this.boundingBox = boundingBox;
        this.unprocessedBoxes = unprocessedBoxes;
        this.requiredOperations = requiredOperations;
    }

    @Override
    public AbstractLeukboxStage advanceStage(ServerLevel serverLevel, Vec3 magneticField, float maxOperatingRadius, float minOperatingTesla) {
        // move fragile blocks (ie block entities, doors, torches, etc.) to a separate storage
        if(!this.unprocessedBoxes.isEmpty()) {
            Pair<SectionPos, BoxedContainer> pair = this.unprocessedBoxes.remove();
            SectionPos sectionPos = pair.left();
            BoxedContainer boxedContainer = pair.right();

            BoundingBox box = boxedContainer.getBox();
            if(box != null) {
                BoxedContainer fragilesContainer = new BoxedContainer();
                BlockState piraceaticTAR = HyphaPiraceaBlocks.PIRACEATIC_TAR.defaultBlockState();

                BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                int mx = sectionPos.minBlockX();
                int my = sectionPos.minBlockY();
                int mz = sectionPos.minBlockZ();
                for (int x = box.minX(); x <= box.maxX(); x++) {
                    for (int y = box.minY(); y <= box.maxY(); y++) {
                        for (int z = box.minZ(); z <= box.maxZ(); z++) {
                            BlockState state = boxedContainer.get(x, y, z);
                            if (state.is(Blocks.STRUCTURE_VOID)) {
                                continue;
                            }
                            mutableBlockPos.set(mx + x, my + y, mz + z);
                            if (StructurePlacer.isStateFragile(state, serverLevel, mutableBlockPos)) {
                                boxedContainer.set(x, y, z, piraceaticTAR);
                                fragilesContainer.set(x, y, z, state);
                            }
                        }
                    }
                }

                if (fragilesContainer.getBox() != null) {
                    this.intermediateStructureStorage.addFragileContainer(sectionPos, fragilesContainer);
                }
            }
        }

        if(this.unprocessedBoxes.isEmpty()) {
            int[] timesBox = calcSpawnTimes(this.boundingBox, this.leukboxPos);
            int[] timesSphere = calcSpawnTimesForSphere(this.leukboxPos.getCenter(), maxOperatingRadius, this.leukboxPos);
            int currentSpawnTime = Math.min(timesBox[1], timesSphere[1]);
            int minSpawnTime = Math.max(timesBox[0], timesSphere[0]);

            if(magneticField.length() >= minOperatingTesla) {
                return new PlaceBlocksStage(this.leukboxPos, this.intermediateStructureStorage, currentSpawnTime, minSpawnTime);
            } else {
                return new InsufficientMagneticFieldStage(this.leukboxPos, this.intermediateStructureStorage, currentSpawnTime, minSpawnTime);
            }
        } else {
            return this;
        }
    }
}
