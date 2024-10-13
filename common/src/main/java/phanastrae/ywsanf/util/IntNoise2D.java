package phanastrae.ywsanf.util;

import net.minecraft.util.Mth;

import java.util.function.Supplier;

public class IntNoise2D {

    protected final int[] data;
    private final int sizeX;
    private final int sizeZ;

    private IntNoise2D(int[] data, int sizeX, int sizeZ) {
        this.data = data;
        this.sizeX = sizeX;
        this.sizeZ = sizeZ;
    }

    private IntNoise2D(int sizeX, int sizeZ) {
        this(new int[sizeX*sizeZ], sizeX, sizeZ);
    }

    public int get(int x, int z) {
        x = Math.floorMod(x, this.sizeX);
        z = Math.floorMod(z, this.sizeZ);
        return getUnsafe(x, z);
    }

    public int getUnsafe(int x, int z) {
        return this.data[x + this.sizeX * z];
    }

    public static IntNoise2D generateNoise(int sizeX, int sizeZ, Supplier<Integer> intSupplier) {
        if(sizeX <= 0 || sizeZ <= 0) {
            sizeX = 0;
            sizeZ = 0;
        }

        IntNoise2D noise = new IntNoise2D(sizeX, sizeZ);

        for(int i = 0; i < noise.data.length; i++) {
            noise.data[i] = intSupplier.get();
        }

        return noise;
    }
}
