package phanastrae.hyphapiracea.block;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import phanastrae.hyphapiracea.electromagnetism.CircuitNetwork;
import phanastrae.hyphapiracea.electromagnetism.CircuitNode;
import phanastrae.hyphapiracea.electromagnetism.CircuitWire;

import java.util.ArrayList;
import java.util.List;

public class MiniCircuit {

    protected final List<WireConnection> outgoingWires;
    protected final List<CircuitWire> internalWires;
    protected final CircuitNode[] directionNodes;
    protected boolean needsUpdate;

    public MiniCircuit() {
        this.outgoingWires = new ObjectArrayList<>();
        this.internalWires = new ObjectArrayList<>();
        this.directionNodes = new CircuitNode[6];
        this.needsUpdate = true;
    }

    @Nullable
    public CircuitNode getNode(Direction direction) {
        int index = direction.get3DDataValue();
        return this.directionNodes[index];
    }

    public void setNode(Direction direction, @Nullable CircuitNode node) {
        int index = direction.get3DDataValue();
        this.directionNodes[index] = node;
    }

    public void bindToNeighbors(Level level, BlockPos thisPos) {
        for(Direction direction : Direction.values()) {
            CircuitNode thisNode = this.getNode(direction);
            if(thisNode == null) {
                continue;
            }

            BlockPos neighborPos = thisPos.offset(direction.getNormal());
            BlockState neighborState = level.getBlockState(neighborPos);
            if(neighborState.getBlock() instanceof MiniCircuitHolder mch) {
                MiniCircuit otherCircuit = mch.getMiniCircuit(level, neighborPos, neighborState, direction.getOpposite());
                if(otherCircuit == null) {
                    continue;
                }

                CircuitNode otherNode = otherCircuit.getNode(direction.getOpposite());
                if(otherNode == null) {
                    continue;
                }

                boolean hasConnection = false;
                for (WireConnection wire : this.outgoingWires) {
                    CircuitNode wireOtherNode = wire.wire.getOtherNode(thisNode);
                    if (wireOtherNode == otherNode) {
                        hasConnection = true;
                        break;
                    }
                }
                if (!hasConnection) {
                    if (thisNode.getNetwork() == null) {
                        thisNode.setNetwork(new CircuitNetwork());
                    }
                    if (otherNode.getNetwork() == null) {
                        otherNode.setNetwork(new CircuitNetwork());
                    }

                    if (thisNode.getNetwork() != otherNode.getNetwork()) {
                        CircuitNetwork network = thisNode.getNetwork();
                        network.merge(otherNode.getNetwork());
                        otherNode.setNetwork(network);
                    }

                    CircuitWire wire = new CircuitWire(thisNode, otherNode, 0, 0);
                    thisNode.getNetwork().addWire(wire);

                    WireConnection wireConnection = new WireConnection(this, otherCircuit, wire);
                    this.addWire(wireConnection);
                    otherCircuit.addWire(wireConnection);
                }
            }
        }
    }

    public void markUpdated() {
        this.needsUpdate = false;
    }

    public boolean needsUpdate() {
        return this.needsUpdate;
    }

    public void onRemoved() {
        if(!this.outgoingWires.isEmpty()) {
            List<WireConnection> copy = new ArrayList<>(this.outgoingWires);
            copy.forEach(WireConnection::remove);
        }

        for(CircuitWire wire : this.internalWires) {
            CircuitNetwork network = wire.getStartNode().getNetwork();
            if(network != null) {
                network.removeWire(wire);
            }
        }
    }

    public void onUnremoved() {
        this.needsUpdate = true;
        for(CircuitWire wire : this.internalWires) {
            CircuitNode start = wire.getStartNode();
            if(start.getNetwork() == null) {
                start.setNetwork(new CircuitNetwork());
            }
            CircuitNode end = wire.getEndNode();
            if(end.getNetwork() == null) {
                end.setNetwork(new CircuitNetwork());
            }
            if(start.getNetwork() != end.getNetwork()) {
                CircuitNetwork network = start.getNetwork();
                network.merge(end.getNetwork());
                end.setNetwork(network);
            }

            CircuitNetwork network = wire.getStartNode().getNetwork();
            if(network != null) {
                network.addWire(wire);
            }
        }
    }

    public void addWire(WireConnection wire) {
        this.outgoingWires.add(wire);
    }

    public void addInternalWire(CircuitWire wire) {
        this.internalWires.add(wire);
    }

    public void removeInternalWire(CircuitWire wire) {
        this.internalWires.remove(wire);
    }

    public static class WireConnection {
        private final MiniCircuit circuit1;
        private final MiniCircuit circuit2;
        private final CircuitWire wire;

        public WireConnection(MiniCircuit circuit1, MiniCircuit circuit2, CircuitWire wire) {
            this.circuit1 = circuit1;
            this.circuit2 = circuit2;
            this.wire = wire;
        }

        public void remove() {
            CircuitNetwork network = this.wire.getStartNode().getNetwork();
            CircuitNetwork network2 = this.wire.getEndNode().getNetwork();
            if(network != null) {
                network.removeWire(this.wire);
            }
            if(network != network2 && network2 != null) {
                network2.removeWire(this.wire);
            }

            this.circuit1.outgoingWires.remove(this);
            this.circuit2.outgoingWires.remove(this);
        }
    }
}
