package phanastrae.ywsanf.electromagnetism;

import net.minecraft.world.phys.Vec3;

public class WireLine {

    private final Vec3 start;
    private Vec3 end;
    private Vec3 startToEnd;
    private double startToEndDistance;
    private Vec3 middle;
    private Vec3 iVec;

    private float current;
    private double magCalcConstant;

    private float dropoffRadius;
    private double dropoffRadiusSqr;
    private double maxPossibleInfluenceRadius;
    private double maxPossibleInfluenceRadiusSqr;

    private float resistancePerBlock;
    private float totalResistance;

    public WireLine(Vec3 start) {
        this.start = start;
        this.end = start;
        this.startToEnd = Vec3.ZERO;
        this.startToEndDistance = 0;
        this.middle = start;
        this.iVec = Vec3.ZERO;

        this.current = 0;
        this.magCalcConstant = 0;

        this.dropoffRadius = 0;
        this.dropoffRadiusSqr = 0;
        this.maxPossibleInfluenceRadiusSqr = 0;

        this.resistancePerBlock = 1F;
        this.totalResistance = 0;
    }

    public void setCurrent(float current) {
        this.current = current;
        this.magCalcConstant = this.current * Electromagnetism.MU_0_BY_FOUR_PI;
    }

    public void setDropoffRadius(float dropoffRadius) {
        this.dropoffRadius = dropoffRadius;
        this.dropoffRadiusSqr = this.dropoffRadius * this.dropoffRadius;

        this.updateMaxInfluenceRadius();
    }

    public void setEndPoint(Vec3 end) {
        this.end = end;
        this.startToEnd = end.subtract(this.start);
        this.startToEndDistance = startToEnd.length();
        this.middle = this.start.add(this.startToEnd.scale(0.5));
        this.iVec = this.startToEnd.normalize();

        this.updateMaxInfluenceRadius();
        this.updateTotalResistance();
    }

    public void setResistancePerBlock(float resistancePerBlock) {
        this.resistancePerBlock = resistancePerBlock;

        this.updateTotalResistance();
    }

    private void updateMaxInfluenceRadius() {
        this.maxPossibleInfluenceRadius = (this.startToEndDistance * 0.5) + this.dropoffRadius;
        this.maxPossibleInfluenceRadiusSqr = this.maxPossibleInfluenceRadius * this.maxPossibleInfluenceRadius;
    }

    public void updateTotalResistance() {
        this.totalResistance = (float)this.startToEndDistance * this.resistancePerBlock;
    }

    public Vec3 getStart() {
        return this.start;
    }

    public Vec3 getEnd() {
        return this.end;
    }

    public Vec3 getStartToEnd() {
        return this.startToEnd;
    }

    public Vec3 getMiddle() {
        return this.middle;
    }

    public Vec3 getIVec() {
        return this.iVec;
    }

    public float getCurrent() {
        return this.current;
    }

    public double getMagCalcConstant() {
        return this.magCalcConstant;
    }

    public float getDropoffRadius() {
        return this.dropoffRadius;
    }

    public double getDropoffRadiusSqr() {
        return this.dropoffRadiusSqr;
    }

    public double getMaxPossibleInfluenceRadius() {
        return this.maxPossibleInfluenceRadius;
    }

    public double getMaxPossibleInfluenceRadiusSqr() {
        return this.maxPossibleInfluenceRadiusSqr;
    }

    public float getResistancePerBlock() {
        return this.resistancePerBlock;
    }

    public float getTotalResistance() {
        return this.totalResistance;
    }

    public boolean canInfluencePoint(Vec3 pos) {
        double dx = pos.x - this.middle.x;
        double dy = pos.y - this.middle.y;
        double dz = pos.z - this.middle.z;
        double radiusSqr = dx*dx + dy*dy + dz*dz;

        return radiusSqr < this.maxPossibleInfluenceRadiusSqr;
    }
}
