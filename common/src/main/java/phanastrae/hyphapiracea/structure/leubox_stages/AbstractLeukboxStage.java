package phanastrae.hyphapiracea.structure.leubox_stages;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractLeukboxStage {
    public static final String TAG_STAGE_ID = "stage_id";
    public static final String TAG_FAKE_STAGE_ID = "fake_stage_id";
    public static final String TAG_ERROR_ID = "error_id";
    public static final String TAG_REQUIRED_OPERATIONS = "required_operations";
    public static final String TAG_CURRENT_SPAWN_TIME = "current_spawn_time";
    public static final String TAG_MIN_SPAWN_TIME = "min_spawn_time";

    protected final BlockPos leukboxPos;
    protected final LeukboxStage stage;
    protected int requiredOperations = 1;

    public AbstractLeukboxStage(BlockPos leukboxPos, LeukboxStage stage) {
        this.leukboxPos = leukboxPos;
        this.stage = stage;
    }

    public abstract AbstractLeukboxStage advanceStage(ServerLevel serverLevel, Vec3 magneticField, float maxOperatingRadius, float minOperatingTesla);

    public LeukboxStage getStage() {
        return this.stage;
    }

    public int getRequiredOperations() {
        return this.requiredOperations;
    }

    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        // empty
        if(nbt.contains(TAG_REQUIRED_OPERATIONS, Tag.TAG_INT)) {
            this.requiredOperations = nbt.getInt(TAG_REQUIRED_OPERATIONS);
        }
    }

    public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        nbt.putString(TAG_STAGE_ID, this.stage.id);
        nbt.putInt(TAG_REQUIRED_OPERATIONS, this.requiredOperations);
    }

    public ErrorStage getError(String errorId) {
        return new ErrorStage(this.leukboxPos, errorId);
    }

    public enum LeukboxStage {
        ERROR("error", 200, false, ChatFormatting.DARK_RED, ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
        IDLE("idle", -1, false),
        GET_STRUCTURE("get_structure", 20, true),
        GET_STRUCTURE_START("get_structure_start", 20, true),
        FILL_STORAGE_PIECES("fill_storage_pieces", 1, true),
        FILL_STORAGE_AFTER("fill_storage_after", 30, true),
        POST_PROCESS("post_process", 1, true),
        PLACE_BLOCKS("place_blocks",1, true, ChatFormatting.GOLD, ChatFormatting.BOLD),
        PLACE_SPECIALS("place_specials", 20, true, ChatFormatting.GOLD, ChatFormatting.BOLD),
        COMPLETED("completed", 60, false, ChatFormatting.GREEN),
        FAKE_CLIENT_STAGE("fake_client_stage", -1, false, ChatFormatting.LIGHT_PURPLE);

        private final String id;
        private final int wait;
        private final boolean showProgress;
        private final ChatFormatting[] formats;

        LeukboxStage(String id, int wait, boolean showProgress, ChatFormatting... formats) {
            this.id = id;
            this.wait = wait;
            this.showProgress = showProgress;
            this.formats = formats;
        }

        public String getId() {
            return this.id;
        }

        public int getWait() {
            return this.wait;
        }

        public boolean shouldShowProgress() {
            return this.showProgress;
        }

        public ChatFormatting[] getFormats() {
            return this.formats;
        }
    }
}
