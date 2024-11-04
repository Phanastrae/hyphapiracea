package phanastrae.hyphapiracea.world;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.duck.LevelDuckInterface;
import phanastrae.hyphapiracea.electromagnetism.Electromagnetism;
import phanastrae.hyphapiracea.electromagnetism.WireLine;
import phanastrae.hyphapiracea.electromagnetism.WorldWireField;
import phanastrae.hyphapiracea.util.MagneticFieldData;

public class HyphaPiraceaLevelAttachment {

    private final Level level;
    private final WorldWireField worldWireField = new WorldWireField();

    public HyphaPiraceaLevelAttachment(Level level) {
        this.level = level;
    }

    public static HyphaPiraceaLevelAttachment getAttachment(Level level) {
        return ((LevelDuckInterface)level).hyphapiracea$getAttachment();
    }

    public void addWire(WireLine wireLine) {
        this.worldWireField.addWireLine(wireLine);
    }

    public void removeWire(WireLine wireLine) {
        this.worldWireField.removeWireLine(wireLine);
    }

    public void updateWire(WireLine wireLine) {
        this.worldWireField.updateWireLine(wireLine);
    }

    public Vec3 getMagneticFieldAtPosition(Vec3 pos) {
        return this.getMagneticFieldAtPosition(pos, false);
    }

    public boolean isPositionWarded(Vec3 pos) {
        MagneticFieldData magneticField = new MagneticFieldData();
        this.worldWireField.forEachWireAffectingPosition(pos, line -> {
            if(!magneticField.insideWardingZone()) {
                Electromagnetism.calculateMagneticFieldFromWireAtPoint(line, pos, magneticField);
            }
        });
        return magneticField.insideWardingZone();
    }

    public Vec3 getMagneticFieldAtPosition(Vec3 pos, boolean obeyWarding) {
        MagneticFieldData magneticField = new MagneticFieldData();
        this.worldWireField.forEachWireAffectingPosition(pos, line -> {
            if(!obeyWarding || !magneticField.insideWardingZone()) {
                Electromagnetism.calculateMagneticFieldFromWireAtPoint(line, pos, magneticField);
            }
        });

        if(obeyWarding && magneticField.insideWardingZone()) {
            return Vec3.ZERO;
        } else {
            Vec3 magField = magneticField.toVec3();
            magField = addNoise(magField);

            return magField;
        }
    }

    public Vec3 getMagneticFieldAtPosition(Vec3 pos, @Nullable WorldWireField.SectionInfo sectionInfo, boolean obeyWarding) {
        MagneticFieldData magneticField = new MagneticFieldData();
        if(sectionInfo != null) {
            sectionInfo.forEach(wireLineHolder -> {
                if(!obeyWarding || !magneticField.insideWardingZone()) {
                    WireLine wireLine = wireLineHolder.wireLine;
                    if (wireLine.canInfluencePoint(pos)) {
                        Electromagnetism.calculateMagneticFieldFromWireAtPoint(wireLine, pos, magneticField);
                    }
                }
            });
        }

        if(obeyWarding && magneticField.insideWardingZone()) {
            return Vec3.ZERO;
        } else {
            Vec3 magField = magneticField.toVec3();
            magField = addNoise(magField);

            return magField;
        }
    }

    public WorldWireField getWorldWireField() {
        return this.worldWireField;
    }

    public Vec3 addNoise(Vec3 magField) {
        // TODO vary noise amount based on height? dimension? point towards poles? strongholds?
        float noiseAmount = 0.000000001F;
        long l = this.level.getRandom().nextLong();

        float x = ((l & 0xFF) / 127.5F - 1F) * noiseAmount;
        float y = (((l & 0xFF00) >> 8) / 127.5F - 1F) * noiseAmount;
        float z = (((l & 0xFF0000) >> 16) / 127.5F - 1F) * noiseAmount;

        return magField.add(x, y, z);
    }
}
