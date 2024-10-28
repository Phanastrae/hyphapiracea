package phanastrae.ywsanf.block.state;

import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class YWSaNFBlockProperties {

    public static final BooleanProperty HAS_DISC = BooleanProperty.create("has_disc");
    public static final BooleanProperty STICKY = BooleanProperty.create("sticky");

    public static final ConductorStateProperty CONDUCTOR_WIRE_STATE = ConductorStateProperty.create(
            "conductor_state",
            ConductorStateProperty.ConductorState.EMPTY,
            ConductorStateProperty.ConductorState.HOLDING_WIRE,
            ConductorStateProperty.ConductorState.ACCEPTING_WIRE
    );
}
