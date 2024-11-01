package phanastrae.ywsanf.electromagnetism;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import phanastrae.ywsanf.util.AugmentedMatrix;

import java.util.*;

public class CircuitNetwork {

    private Collection<CircuitWire> wires = new HashSet<>();
    private boolean needsRecalculation = true;

    public void tick() {
        if(this.needsRecalculation) {
            List<CircuitNetwork> splitNetworks = this.splitNetwork();

            this.recalculate();
            this.needsRecalculation = false;

            for(CircuitNetwork network : splitNetworks) {
                network.recalculate();
                network.needsRecalculation = false;
            }
        }
    }

    public void markNeedsUpdate() {
        this.needsRecalculation = true;
    }

    public List<CircuitNetwork> splitNetwork() {
        Map<CircuitNode, NodeWrapper> map = new HashMap<>();
        Collection<WireWrapper> wireWrappers = new HashSet<>();
        for(CircuitWire wire : this.wires) {
            NodeWrapper start = map.computeIfAbsent(wire.getStartNode(), NodeWrapper::new);
            NodeWrapper end = map.computeIfAbsent(wire.getEndNode(), NodeWrapper::new);

            WireWrapper wireWrapper = new WireWrapper(wire, start, end);
            wireWrappers.add(wireWrapper);

            start.attachWireWrapper(wireWrapper);
            end.attachWireWrapper(wireWrapper);
        }

        List<CircuitNetwork> splitNetworks = new ObjectArrayList<>();

        int remainingNodesToCheck = map.values().size();
        List<NodeWrapper> nodesToCheck = new ObjectArrayList<>();
        List<NodeWrapper> upcomingNodesToCheck = new ObjectArrayList<>();

        List<NodeWrapper> checkedNodes = new ObjectArrayList<>();

        while(remainingNodesToCheck != 0) {
            NodeWrapper startNode = map.values().iterator().next();
            nodesToCheck.add(startNode);
            checkedNodes.add(startNode);
            startNode.setMarked(true);
            remainingNodesToCheck--;

            while (!nodesToCheck.isEmpty()) {
                for (NodeWrapper nodeWrapper : nodesToCheck) {
                    for (WireWrapper wire : nodeWrapper.attachedWireWrappers) {
                        NodeWrapper otherNode = wire.getOtherNode(nodeWrapper);
                        if (!otherNode.getMarked()) {
                            upcomingNodesToCheck.add(otherNode);
                            checkedNodes.add(otherNode);
                            otherNode.setMarked(true);
                            remainingNodesToCheck--;
                        }
                    }
                }
                nodesToCheck.clear();
                nodesToCheck.addAll(upcomingNodesToCheck);
                upcomingNodesToCheck.clear();
            }

            if (remainingNodesToCheck != 0) {
                Collection<WireWrapper> wiresToMove = new HashSet<>();
                for (NodeWrapper nodeWrapper : checkedNodes) {
                    for (WireWrapper wireWrapper : nodeWrapper.attachedWireWrappers) {
                        if (!wiresToMove.contains(wireWrapper)) {
                            wiresToMove.add(wireWrapper);
                        }
                    }
                    map.remove(nodeWrapper.node);
                }
                checkedNodes.clear();

                CircuitNetwork newNetwork = new CircuitNetwork();
                for (WireWrapper wireWrapper : wiresToMove) {
                    wireWrapper.wire.getStartNode().setNetwork(newNetwork);
                    wireWrapper.wire.getEndNode().setNetwork(newNetwork);
                }
                splitNetworks.add(newNetwork);
            }
        }

        return splitNetworks;
    }

    public void recalculate() {
        int nextNodeIndex = 0;
        Map<CircuitNode, NodeWrapper> map = new HashMap<>();
        Collection<WireWrapper> wireWrappers = new HashSet<>();
        for(CircuitWire wire : this.wires) {
            NodeWrapper start;
            CircuitNode startNode = wire.getStartNode();
            if(!map.containsKey(startNode)) {
                start = new NodeWrapper(startNode);
                start.setIndex(nextNodeIndex);
                nextNodeIndex++;
                map.put(startNode, start);
            } else {
                start = map.get(startNode);
            }
            NodeWrapper end;
            CircuitNode endNode = wire.getEndNode();
            if(!map.containsKey(endNode)) {
                end = new NodeWrapper(endNode);
                end.setIndex(nextNodeIndex);
                nextNodeIndex++;
                map.put(endNode, end);
            } else {
                end = map.get(endNode);
            }

            WireWrapper wireWrapper = new WireWrapper(wire, start, end);
            wireWrappers.add(wireWrapper);

            start.attachWireWrapper(wireWrapper);
            end.attachWireWrapper(wireWrapper);
        }
        if(nextNodeIndex == 0) {
            return;
        }

        // TODO combine series/parallel circuits to reduce computation cost

        AugmentedMatrix augmentedMatrix = new AugmentedMatrix(nextNodeIndex, nextNodeIndex);

        for(NodeWrapper nodeWrapper : map.values()) {
            int i = nodeWrapper.index;
            for(WireWrapper wire : nodeWrapper.attachedWireWrappers) {
                NodeWrapper other = wire.getOtherNode(nodeWrapper);
                int j = other.index;
                int sign = wire.endNode == nodeWrapper ? 1 : -1;

                double resistance = wire.wire.getResistance();
                if(resistance != 0) {
                    augmentedMatrix.getMatrix().add(1.0 / resistance, i, j);
                    augmentedMatrix.getMatrix().add(-1.0 / resistance, i, i);
                    augmentedMatrix.getVector().add(sign * -wire.wire.getEmf() / resistance, i);
                }
            }
        }

        solveMatrix(augmentedMatrix);
        augmentedMatrix.getVector().set(0, nextNodeIndex - 1);

        for(WireWrapper wireWrapper : wireWrappers) {
            double startVoltage = augmentedMatrix.getVector().get(wireWrapper.startNode.index);
            double endVoltage = augmentedMatrix.getVector().get(wireWrapper.endNode.index);
            double pd = endVoltage - startVoltage - wireWrapper.wire.getEmf();
            double current = -pd/wireWrapper.wire.getResistance();
            double power = -current * pd;
            wireWrapper.wire.setStats(pd, current, power);
        }
    }

    public static void solveMatrix(AugmentedMatrix augmentedMatrix) {
        // TODO consider looking into alternative faster methods for this
        // TODO consider trying to use the sparseness of the matrix to help
        for(int j = 0; j < augmentedMatrix.getColumns(); j++) {
            double max = 0;
            int maxIndex = j;
            for(int i = j; i < augmentedMatrix.getRows(); i++) {
                double v = augmentedMatrix.getMatrix().get(i, j);
                if(max == 0 || Math.abs(v) > Math.abs(max)) {
                    max = v;
                    maxIndex = i;
                }
            }
            if(Math.abs(max) < 1E-10) {
                continue;
            }
            if(maxIndex != j) {
                augmentedMatrix.swap(maxIndex, j);
            }
            if(j >= augmentedMatrix.getRows()) {
                continue;
            }

            double vj = augmentedMatrix.getMatrix().get(j, j);
            if(vj != 1) {
                augmentedMatrix.multiply(j, 1 / vj);
            }

            for(int i = 0; i < augmentedMatrix.getRows(); i++) {
                if(i != j) {
                    double vi = augmentedMatrix.getMatrix().get(i, j);
                    augmentedMatrix.add(j, i, -vi);
                }
            }
        }
    }

    public void addWire(CircuitWire wire) {
        this.wires.add(wire);
        this.needsRecalculation = true;
    }

    public void removeWire(CircuitWire wire) {
        this.wires.remove(wire);
        this.needsRecalculation = true;
    }

    public void merge(CircuitNetwork other) {
        this.wires.addAll(other.wires);
        for(CircuitWire wire : other.wires) {
            wire.getStartNode().setNetwork(this);
            wire.getEndNode().setNetwork(this);
        }
        other.wires.clear();
        this.needsRecalculation = true;
    }

    public static class NodeWrapper {
        private Collection<WireWrapper> attachedWireWrappers = new ObjectArrayList<>();
        private CircuitNode node;
        private boolean marked = false;
        private int index = -1;

        public NodeWrapper(CircuitNode node) {
            this.node = node;
        }

        public void attachWireWrapper(WireWrapper wireWrapper) {
            this.attachedWireWrappers.add(wireWrapper);
        }

        public void setMarked(boolean value) {
            this.marked = value;
        }

        public boolean getMarked() {
            return this.marked;
        }

        public void setIndex(int index) {
            this.index = index;
        }
    }

    public static class WireWrapper {
        private CircuitWire wire;
        private NodeWrapper startNode;
        private NodeWrapper endNode;

        public WireWrapper(CircuitWire wire, NodeWrapper startNode, NodeWrapper endNode) {
            this.wire = wire;
            this.startNode = startNode;
            this.endNode = endNode;
        }

        public NodeWrapper getStartNode() {
            return this.startNode;
        }

        public NodeWrapper getEndNode() {
            return this.endNode;
        }

        public NodeWrapper getOtherNode(NodeWrapper nodeWrapper) {
            if(nodeWrapper == this.startNode) {
                return this.endNode;
            } else {
                return this.startNode;
            }
        }
    }
}
