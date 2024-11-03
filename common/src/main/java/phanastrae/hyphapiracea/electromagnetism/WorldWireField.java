package phanastrae.hyphapiracea.electromagnetism;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class WorldWireField {

    public Map<WireLine, WireLineHolder> lineHolderMap = new HashMap<>();
    public Map<SectionPos, SectionInfo> sectionInfoMap = new HashMap<>();

    public void addWireLine(WireLine wireLine) {
        if(!this.lineHolderMap.containsKey(wireLine)) {
            WireLineHolder holder = new WireLineHolder(wireLine, this.sectionInfoMap);
            this.lineHolderMap.put(wireLine, holder);
            holder.addToMap();
        }
    }

    public void removeWireLine(WireLine wireLine) {
        if(this.lineHolderMap.containsKey(wireLine)) {
            WireLineHolder holder = this.lineHolderMap.remove(wireLine);
            holder.removeFromMap();
        }
    }

    public void updateWireLine(WireLine wireLine) {
        if(this.lineHolderMap.containsKey(wireLine)) {
            WireLineHolder holder = this.lineHolderMap.get(wireLine);
            holder.update();
        }
    }

    @Nullable
    public SectionInfo getSectionInfoForPosition(Vec3 position) {
        SectionPos sectionPos = SectionPos.of(BlockPos.containing(position));
        return this.getSectionInfoForPosition(sectionPos);
    }

    @Nullable
    public SectionInfo getSectionInfoForPosition(SectionPos sectionPos) {
        if(this.sectionInfoMap.containsKey(sectionPos)) {
            return this.sectionInfoMap.get(sectionPos);
        }

        return null;
    }

    public void forEachWireAffectingPosition(Vec3 position, Consumer<WireLine> consumer) {
        SectionInfo sectionInfo = this.getSectionInfoForPosition(position);
        if(sectionInfo != null) {
            sectionInfo.forEach(wireLineHolder -> {
                WireLine wireLine = wireLineHolder.wireLine;
                if(wireLine.canInfluencePoint(position)) {
                    consumer.accept(wireLine);
                }
            });
        }
    }

    public static class WireLineHolder {

        public final WireLine wireLine;
        private final Map<SectionPos, SectionInfo> sectionInfoMap;
        private BlockBox blockBox;

        public WireLineHolder(WireLine wireLine, Map<SectionPos, SectionInfo> sectionInfoMap) {
            this.wireLine = wireLine;
            this.sectionInfoMap = sectionInfoMap;
            this.blockBox = this.getUpdatedBlockBox();
        }

        public BlockBox getUpdatedBlockBox() {
            BlockPos middle = BlockPos.containing(this.wireLine.getMiddle());
            // expand by sqrt(3) to account for middle getting aligned to a block pos
            int radius = Mth.ceil(this.wireLine.getMaxPossibleInfluenceRadius() + 1.7321);

            BlockPos min = middle.offset(-radius, -radius, -radius);
            BlockPos max = middle.offset(radius, radius, radius);
            return new BlockBox(min, max);
        }

        public void forEachSectionPosInRange(Consumer<SectionPos> consumer) {
            BlockPos min = this.blockBox.min();
            BlockPos max = this.blockBox.max();
            Vec3 middle = this.wireLine.getMiddle();
            double maxRadius = this.wireLine.getMaxPossibleInfluenceRadius();

            SectionPos.betweenClosedStream(min.getX() >> 4, min.getY() >> 4, min.getZ() >> 4, max.getX() >> 4, max.getY() >> 4, max.getZ() >> 4).forEach(sectionPos -> {
                int centerX = sectionPos.minBlockX() + 8;
                int centerY = sectionPos.minBlockY() + 8;
                int centerZ = sectionPos.minBlockZ() + 8;
                double dx = centerX - middle.x;
                double dy = centerY - middle.y;
                double dz = centerZ - middle.z;
                double distSqr = dx*dx + dy*dy + dz*dz;
                double dist = Math.sqrt(distSqr);

                if(dist < maxRadius + 8 * 1.7321) {
                    consumer.accept(sectionPos);
                }
            });
        }

        public void addToMap() {
            this.forEachSectionPosInRange(sectionPos -> {
                SectionInfo sectionInfo;
                if(!this.sectionInfoMap.containsKey(sectionPos)) {
                    sectionInfo = new SectionInfo();
                    this.sectionInfoMap.put(sectionPos, sectionInfo);
                } else {
                    sectionInfo = this.sectionInfoMap.get(sectionPos);
                }

                sectionInfo.addLine(this);
            });
        }

        public void removeFromMap() {
            this.forEachSectionPosInRange(sectionPos -> {
                SectionInfo sectionInfo;
                if(this.sectionInfoMap.containsKey(sectionPos)) {
                    sectionInfo = this.sectionInfoMap.get(sectionPos);

                    sectionInfo.removeLine(this);

                    if(sectionInfo.isEmpty()) {
                        this.sectionInfoMap.remove(sectionPos, sectionInfo);
                    }
                }
            });
        }

        public void update() {
            this.removeFromMap();
            this.blockBox = this.getUpdatedBlockBox();
            this.addToMap();
        }
    }

    public static class SectionInfo {

        private final List<WireLineHolder> affectingLines = new ObjectArrayList<>();

        public void addLine(WireLineHolder wireLineHolder) {
            this.affectingLines.add(wireLineHolder);
        }

        public void removeLine(WireLineHolder wireLineHolder) {
            this.affectingLines.remove(wireLineHolder);
        }

        public boolean isEmpty() {
            return this.affectingLines.isEmpty();
        }

        public void forEach(Consumer<WireLineHolder> consumer) {
            this.affectingLines.forEach(consumer);
        }
    }
}
