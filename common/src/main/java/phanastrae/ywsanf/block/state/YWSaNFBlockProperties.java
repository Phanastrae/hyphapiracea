package phanastrae.ywsanf.block.state;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class YWSaNFBlockProperties {

    public static final BooleanProperty HAS_DISC = BooleanProperty.create("has_disc");
    public static final BooleanProperty STICKY = BooleanProperty.create("sticky");
    public static final BooleanProperty ALWAYS_SHOW_INFO = BooleanProperty.create("always_show_info");
    public static final IntegerProperty STORED_POWER = IntegerProperty.create("stored_power", 0, 15);

    public static final ConductorStateProperty CONDUCTOR_WIRE_STATE = ConductorStateProperty.create(
            "conductor_state",
            ConductorStateProperty.ConductorState.EMPTY,
            ConductorStateProperty.ConductorState.HOLDING_WIRE,
            ConductorStateProperty.ConductorState.ACCEPTING_WIRE
    );
}
