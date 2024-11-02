package phanastrae.ywsanf.electromagnetism;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import phanastrae.ywsanf.util.AugmentedMatrix;

import java.util.*;

public class CircuitNetwork {

    private Collection<CircuitWire> wires = new HashSet<>();
    private boolean needsRecalculation = true;
    private long lastUpdateTime = -1;

    public void tick(long time) {
        if(this.lastUpdateTime != time) {
            this.lastUpdateTime = time;

            if (this.needsRecalculation) {
                //Timer timer = new Timer();
                //timer.start();
                List<CircuitNetwork> splitNetworks = this.splitNetwork();

                this.recalculate();
                this.needsRecalculation = false;

                for (CircuitNetwork network : splitNetworks) {
                    network.recalculate();
                    network.needsRecalculation = false;
                }
                //timer.stop();
                //YWSaNF.LOGGER.info("circuit " + this + " took " + timer.micro() + " microseconds to recalculate");
            }
        }
    }

    public void markNeedsUpdate() {
        this.needsRecalculation = true;
    }

    public List<CircuitNetwork> splitNetwork() {
        // get node wrappers
        Map<CircuitNode, NodeWrapper> map = new HashMap<>();
        for(CircuitWire wire : this.wires) {
            NodeWrapper start = map.computeIfAbsent(wire.getStartNode(), SingleNodeWrapper::new);
            NodeWrapper end = map.computeIfAbsent(wire.getEndNode(), SingleNodeWrapper::new);

            SingleWireWrapper wireWrapper = new SingleWireWrapper(wire, start, end);

            start.attachWireWrapper(wireWrapper);
            end.attachWireWrapper(wireWrapper);
        }
        HashSet<NodeWrapper> nodeWrappers = new HashSet<>(map.values());

        // split network
        List<CircuitNetwork> splitNetworks = new ObjectArrayList<>();

        int remainingNodesToCheck = nodeWrappers.size();
        Queue<NodeWrapper> nodesToCheck = new LinkedList<>();

        List<NodeWrapper> checkedNodes = new ObjectArrayList<>();

        while(remainingNodesToCheck != 0) {
            NodeWrapper startNode = nodeWrappers.iterator().next();

            nodesToCheck.add(startNode);
            checkedNodes.add(startNode);

            startNode.setMarked(true);
            remainingNodesToCheck--;

            while (!nodesToCheck.isEmpty()) {
                NodeWrapper node = nodesToCheck.remove();
                for (WireWrapper wire : node.attachedWireWrappers) {
                    NodeWrapper otherNode = wire.getOtherNode(node);
                    if (!otherNode.getMarked()) {
                        nodesToCheck.add(otherNode);
                        checkedNodes.add(otherNode);

                        otherNode.setMarked(true);
                        remainingNodesToCheck--;
                    }
                }
            }

            if (remainingNodesToCheck != 0) {
                Collection<WireWrapper> wiresToMove = new HashSet<>();
                for (NodeWrapper nodeWrapper : checkedNodes) {
                    for (WireWrapper wireWrapper : nodeWrapper.attachedWireWrappers) {
                        if (!wiresToMove.contains(wireWrapper)) {
                            wiresToMove.add(wireWrapper);
                        }
                    }
                }
                checkedNodes.forEach(nodeWrappers::remove);
                checkedNodes.clear();

                CircuitNetwork newNetwork = new CircuitNetwork();
                for(WireWrapper wire : wiresToMove) {
                    wire.getStartNode().setNetwork(newNetwork);
                    wire.getEndNode().setNetwork(newNetwork);
                    if(wire instanceof SingleWireWrapper sww) {
                        // this should always be true
                        this.removeWire(sww.wire);
                        newNetwork.addWire(sww.wire);
                    }
                }
                splitNetworks.add(newNetwork);
            }
        }

        return splitNetworks;
    }

    public void recalculate() {
        if(this.wires.isEmpty()) {
            return;
        }

        // get node and wire wrappers
        Map<CircuitNode, NodeWrapper> map = new HashMap<>();
        Collection<WireWrapper> wireWrappers = new HashSet<>();
        for(CircuitWire wire : this.wires) {
            NodeWrapper start = map.computeIfAbsent(wire.getStartNode(), SingleNodeWrapper::new);
            NodeWrapper end = map.computeIfAbsent(wire.getEndNode(), SingleNodeWrapper::new);

            WireWrapper wireWrapper = new SingleWireWrapper(wire, start, end);
            start.attachWireWrapper(wireWrapper);
            end.attachWireWrapper(wireWrapper);
            wireWrappers.add(wireWrapper);
        }
        HashSet<NodeWrapper> nodeWrappers = new HashSet<>(map.values());

        // combine any nodes connected by a wire with no resistance
        Queue<WireWrapper> wireQueue = new LinkedList<>(wireWrappers);
        while(!wireQueue.isEmpty()) {
            WireWrapper wireWrapper = wireQueue.remove();

            // TODO support emf != 0 resistance = 0
            if(wireWrapper.getResistance() == 0 && wireWrapper.startNode != wireWrapper.endNode) {
                // create group node
                GroupNodeWrapper groupNode = new GroupNodeWrapper();
                groupNode.addChild(wireWrapper.startNode);
                groupNode.addChild(wireWrapper.endNode);

                // swap out node wrappers
                nodeWrappers.remove(wireWrapper.startNode);
                nodeWrappers.remove(wireWrapper.endNode);
                nodeWrappers.add(groupNode);

                // set all wires attached to nodes inside group node to instead link to the group node
                for (WireWrapper ww : wireWrapper.startNode.attachedWireWrappers) {
                    if (ww == wireWrapper) continue;
                    if (ww.replaceNode(wireWrapper.startNode, groupNode)) {
                        groupNode.attachWireWrapper(ww, true);
                    }
                }
                for (WireWrapper ww : wireWrapper.endNode.attachedWireWrappers) {
                    if (ww == wireWrapper) continue;
                    if (ww.replaceNode(wireWrapper.endNode, groupNode)) {
                        groupNode.attachWireWrapper(ww, true);
                    }
                }

                // set wire to link group node to itself
                wireWrapper.replaceNode(wireWrapper.startNode, groupNode);
                wireWrapper.replaceNode(wireWrapper.endNode, groupNode);
            }
        }

        // combine wires together into series and parallel circuits where possible
        Queue<NodeWrapper> nodeQueue = new LinkedList<>(nodeWrappers);
        while(!nodeQueue.isEmpty()) {
            NodeWrapper node = nodeQueue.remove();
            node.setMarked(true);

            if(node.attachedWireWrappers.size() == 2) {
                WireWrapper wire0 = node.attachedWireWrappers.get(0);
                WireWrapper wire1 = node.attachedWireWrappers.get(1);
                if(wire0 != wire1) {
                    if (wire0.endNode != node) {
                        wire0.invert();
                    }
                    if (wire1.startNode != node) {
                        wire1.invert();
                    }
                    NodeWrapper start = wire0.startNode;
                    NodeWrapper end = wire1.endNode;
                    if(start != end) {

                        SeriesWireWrapper series = new SeriesWireWrapper(List.of(wire0, wire1), start, end);

                        node.attachedWireWrappers.remove(wire0);
                        if (start != node) {
                            start.attachedWireWrappers.remove(wire0);
                        }
                        start.attachWireWrapper(series);
                        if (start.getMarked()) {
                            start.setMarked(false);
                            nodeQueue.add(start);
                        }

                        node.attachedWireWrappers.remove(wire1);
                        if (end != node) {
                            end.attachedWireWrappers.remove(wire1);
                        }
                        end.attachWireWrapper(series);
                        if (end.getMarked()) {
                            end.setMarked(false);
                            nodeQueue.add(end);
                        }

                        if (node != start && node != end) {
                            nodeWrappers.remove(node);
                        }
                        wireWrappers.remove(wire0);
                        wireWrappers.remove(wire1);
                        wireWrappers.add(series);
                    }
                }
            } else if(node.attachedWireWrappers.size() >= 3) {
                // iterate through connected nodes, if any node comes up twice store it as multiTargetNode and break out of the loop
                NodeWrapper multiTargetNode = null;
                for(WireWrapper wireWrapper : node.attachedWireWrappers) {
                    NodeWrapper otherNode = wireWrapper.getOtherNode(node);
                    if(!otherNode.getMarkedAlt()) {
                        otherNode.setMarkedAlt(true);
                    } else {
                        multiTargetNode = otherNode;
                        break;
                    }
                }
                // unmark nodes
                for(WireWrapper wireWrapper : node.attachedWireWrappers) {
                    wireWrapper.getOtherNode(node).setMarkedAlt(false);
                }
                if(multiTargetNode != null) {
                    // create a parallel circuit
                    List<WireWrapper> parallelWires = new ObjectArrayList<>();
                    for(WireWrapper wireWrapper : node.attachedWireWrappers) {
                        NodeWrapper otherNode = wireWrapper.getOtherNode(node);
                        if(otherNode == multiTargetNode) {
                            if(!parallelWires.contains(wireWrapper)) {
                                parallelWires.add(wireWrapper);
                                if(wireWrapper.startNode != node) {
                                    wireWrapper.invert();
                                }
                            }
                        }
                    }

                    ParallelWireWrapper parallel = new ParallelWireWrapper(parallelWires, node, multiTargetNode);

                    node.attachWireWrapper(parallel);
                    if(multiTargetNode != node) {
                        multiTargetNode.attachWireWrapper(parallel);
                    }
                    for(WireWrapper wireWrapper : parallelWires) {
                        node.attachedWireWrappers.remove(wireWrapper);
                        if(multiTargetNode != node) {
                            multiTargetNode.attachedWireWrappers.remove(wireWrapper);
                        }
                    }
                    wireWrappers.removeAll(parallelWires);
                    wireWrappers.add(parallel);

                    node.setMarked(false);
                    nodeQueue.add(node);
                    if(multiTargetNode.getMarked()) {
                        multiTargetNode.setMarked(false);
                        nodeQueue.add(multiTargetNode);
                    }
                }
            }
        }

        // enumerate nodes
        int nextNodeIndex = 0;
        for(NodeWrapper nodeWrapper : nodeWrappers) {
            nodeWrapper.setIndex(nextNodeIndex);
            nextNodeIndex++;
        }

        // form a system of linear equations
        AugmentedMatrix augmentedMatrix = new AugmentedMatrix(nextNodeIndex, nextNodeIndex);
        for(NodeWrapper nodeWrapper : nodeWrappers) {
            int i = nodeWrapper.index;
            for(WireWrapper wire : nodeWrapper.attachedWireWrappers) {
                NodeWrapper other = wire.getOtherNode(nodeWrapper);
                int j = other.index;
                int sign = wire.endNode == nodeWrapper ? 1 : -1;

                double resistance = wire.getResistance();
                if(resistance != 0) {
                    augmentedMatrix.getMatrix().add(1.0 / resistance, i, j);
                    augmentedMatrix.getMatrix().add(-1.0 / resistance, i, i);
                    augmentedMatrix.getVector().add(sign * -wire.getEmf() / resistance, i);
                }
            }
        }

        // solve the matrix
        solveMatrix(augmentedMatrix);

        // set voltages, including setting any unconstrained voltages to zero
        for(NodeWrapper nodeWrapper : nodeWrappers) {
            int index = nodeWrapper.index;
            double value = augmentedMatrix.getMatrix().get(index, index);
            if(Math.abs(value) < 1E-4) {
                nodeWrapper.setVoltage(0);
            } else {
                double voltage = augmentedMatrix.getVector().get(index);
                nodeWrapper.setVoltage(voltage);
            }
        }

        // use the results to update the wires
        for(WireWrapper wireWrapper : wireWrappers) {
            double startVoltage = wireWrapper.getStartNode().getVoltage();
            double endVoltage = wireWrapper.getEndNode().getVoltage();
            double pd = startVoltage - endVoltage;
            wireWrapper.setStats(pd);
        }
    }

    public static void solveMatrix(AugmentedMatrix augmentedMatrix) {
        // use Gaussian Elimination to solve the system of equations

        for(int j = 0; j < augmentedMatrix.getColumns(); j++) {
            double max = 0;
            int maxIndex = j;
            for(int i = j; i < augmentedMatrix.getRows(); i++) {
                double Mij;
                // the marker nonsense here is used to avoid the O(n) lookup time on the linked list matrix
                // as the column only ever increases we can just mark the last column visited and check that on our next go around
                AugmentedMatrix.MatrixRow row = augmentedMatrix.getMatrix().getRow(i);
                AugmentedMatrix.IndexValuePair markedLast = row.getMarked();
                AugmentedMatrix.IndexValuePair marked = null;
                if(markedLast == null) {
                    markedLast = row.getFirst();
                }
                if(markedLast != null) {
                    marked = markedLast.getNext();
                }
                while(marked != null && marked.getIndex() <= j) {
                    markedLast = marked;
                    marked = marked.getNext();
                }
                if(markedLast == null) {
                    Mij = 0;
                } else if(markedLast.getIndex() == j) {
                    Mij = markedLast.getValue();
                } else {
                    Mij = 0;
                }
                row.setMarked(markedLast);

                if(max == 0 || Math.abs(Mij) > Math.abs(max)) {
                    max = Mij;
                    maxIndex = i;
                }
            }
            if(Math.abs(max) < 1E-10) {
                continue;
            }
            if(maxIndex != j) {
                augmentedMatrix.swap(maxIndex, j);
                //YWSaNF.LOGGER.info("swap rows " + maxIndex + " and " + j);
                //YWSaNF.LOGGER.info(augmentedMatrix.toString());
            }
            if(j >= augmentedMatrix.getRows()) {
                continue;
            }

            double vj = augmentedMatrix.getMatrix().get(j, j);
            if(vj != 1) {
                augmentedMatrix.multiply(j, 1 / vj);
                //YWSaNF.LOGGER.info("multiply row "+ j + " by 1 / " + vj);
                //YWSaNF.LOGGER.info(augmentedMatrix.toString());
            }

            for(int i = 0; i < augmentedMatrix.getRows(); i++) {
                if(i != j) {
                    // O(n) lookup here is probably fine since add is also O(n) and needed for gaussian elimination
                    double vi = augmentedMatrix.getMatrix().get(i, j);
                    if(Math.abs(vi) > 1E-8) {
                        augmentedMatrix.add(j, i, -vi);
                        //YWSaNF.LOGGER.info("add " + -vi + " copies of row " + j + " to row " + i);
                        //YWSaNF.LOGGER.info(augmentedMatrix.toString());
                    }
                }
            }
        }
    }

    public void addWire(CircuitWire wire) {
        this.wires.add(wire);
        this.needsRecalculation = true;
    }

    public void removeWire(CircuitWire wire) {
        if(this.wires.remove(wire)) {
            this.needsRecalculation = true;
        }
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

    public abstract static class NodeWrapper {
        private List<WireWrapper> attachedWireWrappers = new ObjectArrayList<>();
        private byte marked = 0;
        private int index = -1;
        private double voltage = 0;

        public void attachWireWrapper(WireWrapper wireWrapper) {
            this.attachWireWrapper(wireWrapper, false);
        }

        public void attachWireWrapper(WireWrapper wireWrapper, boolean checkIfPresent) {
            if(!checkIfPresent || !this.attachedWireWrappers.contains(wireWrapper)) {
                this.attachedWireWrappers.add(wireWrapper);
            }
        }

        public void setMarked(boolean value) {
            this.marked = (byte)(value ? (this.marked | 0x1) : (this.marked & 0x2));
        }

        public boolean getMarked() {
            return (this.marked & 0x1) == 0x1;
        }

        public void setMarkedAlt(boolean value) {
            this.marked = (byte)(value ? (this.marked | 0x2) : (this.marked & 0x1));
        }

        public boolean getMarkedAlt() {
            return (this.marked & 0x2) == 0x2;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public void setVoltage(double voltage) {
            this.voltage = voltage;
        }

        public double getVoltage() {
            return voltage;
        }

        public abstract void setNetwork(CircuitNetwork network);
    }

    public static class SingleNodeWrapper extends NodeWrapper {
        private final CircuitNode node;

        public SingleNodeWrapper(CircuitNode node) {
            this.node = node;
        }

        @Override
        public void setNetwork(CircuitNetwork network) {
            this.node.setNetwork(network);
        }
    }

    public static class GroupNodeWrapper extends NodeWrapper {
        private final List<NodeWrapper> children = new ObjectArrayList<>();

        public void addChild(NodeWrapper child) {
            this.children.add(child);
        }

        @Override
        public void setNetwork(CircuitNetwork network) {
            this.children.forEach(nodeWrapper -> nodeWrapper.setNetwork(network));
        }

        @Override
        public void setVoltage(double voltage) {
            super.setVoltage(voltage);
            for(NodeWrapper child : this.children) {
                child.setVoltage(voltage);
            }
        }
    }

    public abstract static class WireWrapper {
        private NodeWrapper startNode;
        private NodeWrapper endNode;
        private final double resistance;
        private double emf;

        public WireWrapper(NodeWrapper startNode, NodeWrapper endNode, double resistance, double emf) {
            this.startNode = startNode;
            this.endNode = endNode;
            this.resistance = resistance;
            this.emf = emf;
        }

        public void invert() {
            NodeWrapper start = this.startNode;
            this.startNode = this.endNode;
            this.endNode = start;
            this.emf *= -1;
        }

        public boolean replaceNode(NodeWrapper oldNode, NodeWrapper newNode) {
            boolean changed = false;
            if(oldNode == this.startNode) {
                this.startNode = newNode;
                changed = true;
            }
            if(oldNode == this.endNode) {
                this.endNode = newNode;
                changed = true;
            }
            return changed;
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

        public double getResistance() {
            return this.resistance;
        }

        public double getEmf() {
            return this.emf;
        }

        public abstract void setStats(double pd);
    }

    public static class SingleWireWrapper extends WireWrapper {
        private final CircuitWire wire;
        private boolean flipped = false;

        public SingleWireWrapper(CircuitWire wire, NodeWrapper startNode, NodeWrapper endNode) {
            super(startNode, endNode, wire.getResistance(), wire.getEmf());
            this.wire = wire;
        }

        @Override
        public void invert() {
            super.invert();
            this.flipped = !this.flipped;
        }

        @Override
        public void setStats(double pd) {
            if(this.flipped) {
                pd *= -1;
            }
            double v = pd + wire.getEmf();
            if(Math.abs(v) < 1E-4) {
                this.wire.setStats(0, 0, 0);
            } else {
                double current = v / this.getResistance();
                double power = current * v;
                this.wire.setStats(v, current, power);
            }
        }
    }

    public static class SeriesWireWrapper extends WireWrapper {
        private List<WireWrapper> wires;

        private SeriesWireWrapper(List<WireWrapper> wires, NodeWrapper startNode, NodeWrapper endNode) {
            // wires should be given in order from start to end
            super(startNode, endNode, calculateResistance(wires), calculateEmf(wires));
            this.wires = wires;
        }

        private static double calculateResistance(List<WireWrapper> wires) {
            double tot = 0;
            for(WireWrapper wire : wires) {
                tot += wire.getResistance();
            }
            return tot;
        }

        private static double calculateEmf(List<WireWrapper> wires) {
            double tot = 0;
            for(WireWrapper wire : wires) {
                tot += wire.getEmf();
            }
            return tot;
        }

        @Override
        public void setStats(double pd) {
            double v = pd + this.getEmf();
            double current = v / this.getResistance();

            for(WireWrapper wire : this.wires) {
                double dv = current * wire.getResistance() - wire.getEmf();
                wire.setStats(dv);
            }
        }

        @Override
        public void invert() {
            super.invert();
            this.wires.forEach(WireWrapper::invert);
        }
    }

    public static class ParallelWireWrapper extends WireWrapper {
        private List<WireWrapper> wires;

        private ParallelWireWrapper(List<WireWrapper> wires, NodeWrapper startNode, NodeWrapper endNode) {
            // wires should all be oriented from start to end
            super(startNode, endNode, calculateResistance(wires), calculateEmf(wires));
            this.wires = wires;
        }

        private static double calculateResistance(List<WireWrapper> wires) {
            double tot = 0;
            for(WireWrapper wire : wires) {
                tot += 1 / wire.getResistance();
            }
            return 1 / tot;
        }

        private static double calculateEmf(List<WireWrapper> wires) {
            double tot1 = 0;
            double tot2 = 0;
            for(WireWrapper wire : wires) {
                tot1 += wire.getEmf() / wire.getResistance();
                tot2 += 1 / wire.getResistance();
            }
            return tot1 / tot2;
        }

        @Override
        public void setStats(double pd) {
            this.wires.forEach(wire -> wire.setStats(pd));
        }

        @Override
        public void invert() {
            super.invert();
            this.wires.forEach(WireWrapper::invert);
        }
    }
}
