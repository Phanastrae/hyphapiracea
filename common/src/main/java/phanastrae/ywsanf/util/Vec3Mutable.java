package phanastrae.ywsanf.util;

import net.minecraft.world.phys.Vec3;

public class Vec3Mutable {

    private double x;
    private double y;
    private double z;

    public Vec3Mutable(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3Mutable() {
        this(0.0, 0.0, 0.0);
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void add(double dx, double dy, double dz) {
        this.x += dx;
        this.y += dy;
        this.z += dz;
    }

    public double lengthSqr() {
        return this.x * this.x + this.y * this.y + this.z * this.z;
    }

    public double length() {
        return Math.sqrt(this.lengthSqr());
    }

    public Vec3 toVec3() {
        return new Vec3(this.x, this.y, this.z);
    }
}
