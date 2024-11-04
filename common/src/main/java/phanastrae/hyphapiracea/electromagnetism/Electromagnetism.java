package phanastrae.hyphapiracea.electromagnetism;

import net.minecraft.world.phys.Vec3;
import phanastrae.hyphapiracea.util.MagneticFieldData;

public class Electromagnetism {
    public static final double MU_0 = 1.256637061E-6;
    public static final double PI = Math.PI;
    public static final double MU_0_BY_FOUR_PI = MU_0 / (4 * PI);
    public static final double WIRE_RADIUS = 1/16.0;
    public static final double WIRE_RADIUS_SQR = WIRE_RADIUS * WIRE_RADIUS;
    public static final double ONE_BY_WIRE_RADIUS_SQR = 1 / WIRE_RADIUS_SQR;

    public static void calculateMagneticFieldFromWireAtPoint(WireLine wire, Vec3 particlePos, MagneticFieldData out) {
        // everything here is written here with primitives and as few(ish) operations as possible to try and make it go zoom

        // get precalculated info from the wire
        Vec3 startPos = wire.getStart();
        Vec3 endPos = wire.getEnd();
        Vec3 iVec = wire.getIVec();
        double mcc = wire.getMagCalcConstant();
        double dropoffRadiusSqr = wire.getDropoffRadiusSqr();
        double wardingRadiusSqr = wire.getWardingRadiusSqr();

        // calculate the relative start position: a = startPos - particlePos
        double ax = startPos.x - particlePos.x;
        double ay = startPos.y - particlePos.y;
        double az = startPos.z - particlePos.z;

        double aSqrLength = ax*ax + ay*ay + az*az;
        double aLength = Math.sqrt(aSqrLength);
        double aDotI = ax * iVec.x + ay * iVec.y + az * iVec.z;

        // calculate the relative end position: b = endPos - particlePos
        double bx = endPos.x - particlePos.x;
        double by = endPos.y - particlePos.y;
        double bz = endPos.z - particlePos.z;

        double bSqrLength = bx*bx + by*by + bz*bz;
        double bLength = Math.sqrt(bSqrLength);
        double bDotI = bx * iVec.x + by * iVec.y + bz * iVec.z;

        // u = closest point on wire to p
        // the unit vector j = -u/length(u), and is not explicitly calculate anywhere here
        double ux = bx - iVec.x * bDotI;
        double uy = by - iVec.y * bDotI;
        double uz = bz - iVec.z * bDotI;

        double uSqrLength = ux * ux + uy * uy + uz * uz;

        // calculate the distance to the wire
        double distSqr;
        if(aDotI <= 0 && bDotI <= 0) {
            // closest point on wire is the endpoint with highest dotI
            if(bDotI <= aDotI) {
                distSqr = aSqrLength;
            } else {
                distSqr = bSqrLength;
            }
        } else if(aDotI >= 0 && bDotI >= 0) {
            // closest point on wire is the endpoint with lowest dotI
            if(aDotI <= bDotI) {
                distSqr = aSqrLength;
            } else {
                distSqr = bSqrLength;
            }
        } else {
            // closest point on wire is inside the wire
            distSqr = uSqrLength;
        }

        // if outside radius, add nothing, and so exit early
        if(distSqr <= wardingRadiusSqr) {
            out.setInsideWardingZone(true);
        }
        if(distSqr >= dropoffRadiusSqr) {
            return;
        }
        // calculate ratio, will be in range [0,1]
        double radiusRatio = distSqr / dropoffRadiusSqr;
        // calculate dropoff factor
        // the factor used here is (1 - radiusRatio^4)^4 but this is fairly arbitrary
        double rr2 = radiusRatio * radiusRatio;
        double rr4 = rr2 * rr2;
        double omrr4 = 1 - rr4;
        double omrr4_2 = omrr4 * omrr4;
        double dropoffFactor = omrr4_2 * omrr4_2;

        // unit vector k is cross product of i, j
        // here we instead calculate k * length(u) = i x j * length(u) = i x -u/length(u) * length(u) = -(i x u)
        double kXlU = -iVec.y * uz + iVec.z * uy;
        double kYlU = -iVec.z * ux + iVec.x * uz;
        double kZlU = -iVec.x * uy + iVec.y * ux;

        // avoid various divide by zero errors by switching from scaling by 1/r to scaling by r
        // this may or may not actually be physically accurate, but that probably doesn't matter
        double aLengthFactor = aSqrLength <= WIRE_RADIUS_SQR ? (aLength * ONE_BY_WIRE_RADIUS_SQR) : (1.0 / aLength);
        double bLengthFactor = bSqrLength <= WIRE_RADIUS_SQR ? (bLength * ONE_BY_WIRE_RADIUS_SQR) : (1.0 / bLength);
        double uLengthFactor = uSqrLength <= WIRE_RADIUS_SQR ? ONE_BY_WIRE_RADIUS_SQR : (1.0 / uSqrLength);

        // calculate b.i/length(b) - a.i/length(a) = norm(b).i - norm(a).i
        double normDotIDifference = bDotI * bLengthFactor - aDotI * aLengthFactor;

        // calculate the combined factor of (magnetic field magnitude) * (1 / length(u)) * dropoffFactor
        // ie 'magnetic Field Magnitude Divided by length(u) with Drop off Factor' : 'magFieldMagnitudeDivblUwDoF'
        // magnetic field magnitude = mcc * normDotIDifference / length(u)
        // mcc = mu_0 * current / (4 * pi)
        double magFieldMagnitudeDivblUwDoF = mcc * normDotIDifference * uLengthFactor * dropoffFactor;

        // calculate the magnetic field vector B
        // the factors of length(u) and 1 / length(u) cancel out here
        double magFieldX = magFieldMagnitudeDivblUwDoF * kXlU;
        double magFieldY = magFieldMagnitudeDivblUwDoF * kYlU;
        double magFieldZ = magFieldMagnitudeDivblUwDoF * kZlU;

        // add to total
        out.add(magFieldX, magFieldY, magFieldZ);
    }

    public static Vec3 calculateForce(Vec3 magneticField, Vec3 velocity, double electricCharge, double magneticCharge) {
        Vec3 eForce = electricCharge == 0 ? Vec3.ZERO : velocity.cross(magneticField).scale(electricCharge);
        Vec3 mForce = magneticCharge == 0 ? Vec3.ZERO : magneticField.scale(magneticCharge / MU_0);
        return eForce.add(mForce);
    }
}
