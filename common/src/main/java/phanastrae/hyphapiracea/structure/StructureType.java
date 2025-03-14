package phanastrae.hyphapiracea.structure;

import net.minecraft.util.StringRepresentable;

public enum StructureType implements StringRepresentable {
    STRUCTURE("structure"),
    TEMPLATE("template");

    private final String name;

    StructureType(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }
}
