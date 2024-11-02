package phanastrae.hyphapiracea.block.state;

import com.google.common.collect.Lists;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Collection;

public class ConductorStateProperty extends EnumProperty<ConductorStateProperty.ConductorState> {

    protected ConductorStateProperty(String name, Collection<ConductorState> values) {
        super(name, ConductorState.class, values);
    }

    public static ConductorStateProperty create(String name, ConductorState... values) {
        return create(name, Lists.newArrayList(values));
    }

    public static ConductorStateProperty create(String name, Collection<ConductorState> values) {
        return new ConductorStateProperty(name, values);
    }

    public enum ConductorState implements StringRepresentable {
        EMPTY("empty"),
        HOLDING_WIRE("holding_wire"),
        ACCEPTING_WIRE("accepting_wire");

        private final String name;

        ConductorState(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
