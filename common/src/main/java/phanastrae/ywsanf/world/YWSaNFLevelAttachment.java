package phanastrae.ywsanf.world;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import phanastrae.ywsanf.duck.LevelDuckInterface;
import phanastrae.ywsanf.electromagnetism.Electromagnetism;
import phanastrae.ywsanf.electromagnetism.WireLine;
import phanastrae.ywsanf.electromagnetism.WorldWireField;
import phanastrae.ywsanf.util.Vec3Mutable;

public class YWSaNFLevelAttachment {

    private final Level level;
    private final WorldWireField worldWireField = new WorldWireField();

    public YWSaNFLevelAttachment(Level level) {
        this.level = level;
    }

    public static YWSaNFLevelAttachment getAttachment(Level level) {
        return ((LevelDuckInterface)level).ywsanf$getAttachment();
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
        Vec3Mutable magneticField = new Vec3Mutable();
        this.worldWireField.forEachWireAffectingPosition(pos, line -> Electromagnetism.calculateMagneticFieldFromWireAtPoint(line, pos, magneticField));

        Vec3 magField = magneticField.toVec3();

        magField = addNoise(magField);

        return magField;
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
