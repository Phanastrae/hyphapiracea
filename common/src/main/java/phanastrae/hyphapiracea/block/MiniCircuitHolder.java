package phanastrae.hyphapiracea.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Queue;

public interface MiniCircuitHolder {

    @Nullable
    MiniCircuit getMiniCircuit(Level level, BlockPos pos, BlockState state, Direction side);

    static void updateIfNeeded(Level level, BlockPos thisPos, Direction side) {
        BlockState thisState = level.getBlockState(thisPos);
        if(thisState.getBlock() instanceof MiniCircuitHolder mch) {
            MiniCircuit mc = mch.getMiniCircuit(level, thisPos, thisState, side);
            if(mc == null || !mc.needsUpdate()) {
                return;
            }
            updateIfNeeded(level, thisPos, mc);
        }
    }

    static void updateIfNeeded(Level level, BlockPos thisPos, MiniCircuit mc) {
        if(!mc.needsUpdate()) {
            return;
        }

        mc.markUpdated();
        mc.bindToNeighbors(level, thisPos);

        Queue<BlockPos> posQueue = new LinkedList<>();
        posQueue.add(thisPos);
        while(!posQueue.isEmpty()) {
            BlockPos pos = posQueue.remove();

            for(Direction direction : Direction.values()) {
                BlockPos neighborPos = pos.offset(direction.getNormal());
                BlockState neighborState = level.getBlockState(neighborPos);

                if(neighborState.getBlock() instanceof MiniCircuitHolder mch2) {
                    MiniCircuit mc2 = mch2.getMiniCircuit(level, neighborPos, neighborState, direction.getOpposite());
                    if(mc2 != null && mc2.needsUpdate()) {
                        mc2.markUpdated();
                        mc2.bindToNeighbors(level, neighborPos);

                        posQueue.add(neighborPos);
                    }
                }
            }
        }
    }
}
