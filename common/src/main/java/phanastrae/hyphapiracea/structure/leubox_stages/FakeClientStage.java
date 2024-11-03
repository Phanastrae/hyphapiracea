package phanastrae.hyphapiracea.structure.leubox_stages;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class FakeClientStage extends AbstractLeukboxStage implements SpawnTimeHolder, ErrorIdHolder {
    private final LeukboxStage fakeStage;
    private int currentSpawnTime;
    private final int minSpawnTime;
    private final String errorId;

    public FakeClientStage(BlockPos leukboxPos, LeukboxStage fakeStage, int requiredOperations, int currentSpawnTime, int minSpawnTime, String errorId) {
        super(leukboxPos, LeukboxStage.FAKE_CLIENT_STAGE);

        this.fakeStage = fakeStage;
        this.requiredOperations = requiredOperations;

        this.currentSpawnTime = currentSpawnTime;
        this.minSpawnTime = minSpawnTime;
        this.errorId = errorId;
    }

    @Override
    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.saveAdditional(nbt, registryLookup);
        nbt.putString(TAG_FAKE_STAGE_ID, this.fakeStage.getId());
        nbt.putInt(TAG_CURRENT_SPAWN_TIME, this.currentSpawnTime);
        nbt.putInt(TAG_MIN_SPAWN_TIME, this.minSpawnTime);
        nbt.putString(TAG_ERROR_ID, this.errorId);
    }

    public FakeClientStage advanceStage() {
        if(this.currentSpawnTime >= this.minSpawnTime) {
            this.currentSpawnTime--;
        }
        return this;
    }

    @Override
    public AbstractLeukboxStage advanceStage(ServerLevel serverLevel, Vec3 magneticField, float maxOperatingRadius, float minOperatingTesla) {
        return this.advanceStage();
    }

    @Override
    public int getCurrentSpawnTime() {
        return this.currentSpawnTime;
    }

    @Override
    public int getMinSpawnTime() {
        return this.minSpawnTime;
    }

    @Override
    public LeukboxStage getStage() {
        return this.fakeStage;
    }

    @Override
    public String getErrorId() {
        return this.errorId;
    }
}
