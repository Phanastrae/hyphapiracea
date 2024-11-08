package phanastrae.hyphapiracea.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class AmmeterBlockEntity extends AbstractTwoSidedCircuitComponentBlockEntity implements ClientHighlightReactingBlockEntity {
    public static final String KEY_CURRENT = "current";

    public long lastHighlightTime = -1;
    private int lastComparatorOutput = -1;
    private double current;

    public AmmeterBlockEntity(BlockPos pos, BlockState blockState) {
        super(HyphaPiraceaBlockEntityTypes.HYPHAL_AMMETER, pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        if(nbt.contains(KEY_CURRENT, Tag.TAG_DOUBLE)) {
            this.current = nbt.getDouble(KEY_CURRENT);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        CompoundTag nbt = super.getUpdateTag(registryLookup);
        nbt.putDouble(KEY_CURRENT, this.current);
        return nbt;
    }

    public double getCurrent() {
        return this.current;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AmmeterBlockEntity blockEntity) {
        AbstractTwoSidedCircuitComponentBlockEntity.serverTick(level, pos, state, blockEntity);

        double current = blockEntity.wire.getCurrent();
        if(blockEntity.current != current || blockEntity.lastComparatorOutput == -1) {
            blockEntity.current = current;
            blockEntity.lastComparatorOutput = blockEntity.calculateComparatorOutput();
            blockEntity.sendUpdate();
            blockEntity.setChanged();
        }
    }

    public int calculateComparatorOutput() {
        int comparatorOutput = 0;
        double strengthToBeat = 0.25;
        double current = this.current;
        for(int i = 0; i < 15; i++) {
            if(current >= strengthToBeat) {
                strengthToBeat *= 2;
                comparatorOutput += 1;
            } else {
                break;
            }
        }

        return comparatorOutput;
    }

    public int getComparatorOutput() {
        if(this.lastComparatorOutput == -1) {
            this.lastComparatorOutput = this.calculateComparatorOutput();
            this.setChanged();
        }

        return this.lastComparatorOutput;
    }

    @Override
    public void onHighlight() {
        if(this.getLevel() != null) {
            this.lastHighlightTime = this.getLevel().getGameTime();
        }
    }
}
