package phanastrae.ywsanf.electromagnetism;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class ChargeSac {
    private static final String CHARGE_MC_KEY = "chargeMilliCoulombs";

    private long chargeMilliCoulombs;
    private long maxChargeMilliCoulombs;
    @Nullable
    private Runnable onUpdate;

    public ChargeSac() {
        this.chargeMilliCoulombs = 0;
        this.maxChargeMilliCoulombs = 1000000000;
    }

    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        if(nbt.contains(CHARGE_MC_KEY, Tag.TAG_LONG)) {
            this.chargeMilliCoulombs = nbt.getLong(CHARGE_MC_KEY);
        }
    }

    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.putLong(CHARGE_MC_KEY, this.chargeMilliCoulombs);
    }

    public void sendUpdate() {
        if(this.onUpdate != null) {
            this.onUpdate.run();
        }
    }

    public void setOnUpdate(@Nullable Runnable onUpdate) {
        this.onUpdate = onUpdate;
    }

    public void addCharge(long milliCoulombs) {
        this.chargeMilliCoulombs += milliCoulombs;
    }

    public long getChargeMilliCoulombs() {
        return this.chargeMilliCoulombs;
    }

    public long getMaxChargeMilliCoulombs() {
        return this.maxChargeMilliCoulombs;
    }

    public double getVoltage() {
        // the specific value here is just chosen because it works
        return this.chargeMilliCoulombs * 0.0001;
    }

    public static long getChargeDeltaMilliCoulombs(ChargeSac startSac, ChargeSac endSac, float resistance) {
        if(startSac != null && endSac != null) {
            long startChargeMC = startSac.getChargeMilliCoulombs();
            long endChargeMC = endSac.getChargeMilliCoulombs();

            long deltaChargeMC = endChargeMC - startChargeMC;
            long maxTransferMC = (deltaChargeMC >= 0 ? deltaChargeMC : -deltaChargeMC) / 2;

            double voltageDifference = endSac.getVoltage() - startSac.getVoltage();
            double currentAmps = voltageDifference / resistance;
            double currentMilliCoulombPerTick = currentAmps * 1000 / 20;

            long deltaCharge = Math.min(Mth.ceil(Math.abs(currentMilliCoulombPerTick)), maxTransferMC) * Mth.sign(currentMilliCoulombPerTick);
            deltaCharge = limitDeltaCharge(deltaCharge, startSac, endSac);
            return deltaCharge;
        }

        return 0;
    }

    public static long limitDeltaCharge(long deltaCharge, ChargeSac startSac, ChargeSac endSac) {
        long startChargeMC = startSac.getChargeMilliCoulombs();
        long endChargeMC = endSac.getChargeMilliCoulombs();

        long startMaxCharge = startSac.getMaxChargeMilliCoulombs();
        long endMaxCharge = startSac.getMaxChargeMilliCoulombs();

        if(startChargeMC + deltaCharge >= startMaxCharge) {
            deltaCharge = startMaxCharge - startChargeMC;
        }
        if(startChargeMC + deltaCharge <= -startMaxCharge) {
            deltaCharge = -startMaxCharge - startChargeMC;
        }
        if(endChargeMC - deltaCharge >= endMaxCharge) {
            deltaCharge = -endMaxCharge + endChargeMC;
        }
        if(endChargeMC - deltaCharge <= -endMaxCharge) {
            deltaCharge = endChargeMC + endMaxCharge;
        }

        return deltaCharge;
    }
}
