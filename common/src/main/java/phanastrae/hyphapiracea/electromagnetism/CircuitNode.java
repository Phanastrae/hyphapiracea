package phanastrae.hyphapiracea.electromagnetism;

import org.jetbrains.annotations.Nullable;

public class CircuitNode {
    @Nullable
    private CircuitNetwork network;

    public void setNetwork(CircuitNetwork network) {
        this.network = network;
    }

    public CircuitNetwork getNetwork() {
        return this.network;
    }
}
