package phanastrae.ywsanf.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import phanastrae.ywsanf.electromagnetism.CircuitNode;

public interface CircuitNodeHolder {

    @Nullable
    CircuitNode getCircuitNode(Level level, BlockPos pos, BlockState state, Direction side);
}
