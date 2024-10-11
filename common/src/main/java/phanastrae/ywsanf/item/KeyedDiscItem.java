package phanastrae.ywsanf.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import phanastrae.ywsanf.structure.StructurePlacement;

public class KeyedDiscItem extends Item {
    public KeyedDiscItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        } else {
            BlockPos pos = context.getClickedPos();
            BlockState state = level.getBlockState(pos);

            BlockPos offsetPos;
            if (state.getCollisionShape(level, pos).isEmpty()) {
                offsetPos = pos;
            } else {
                Direction direction = context.getClickedFace();
                offsetPos = pos.relative(direction);
            }

            StructurePlacement.placeStructure(serverLevel, offsetPos);

            return InteractionResult.CONSUME;
        }
    }
}
