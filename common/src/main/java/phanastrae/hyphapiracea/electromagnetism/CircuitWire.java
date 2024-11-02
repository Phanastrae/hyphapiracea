package phanastrae.hyphapiracea.electromagnetism;

public class CircuitWire {
    private CircuitNode startNode;
    private CircuitNode endNode;
    private double resistance;
    private double emf;

    private double voltage;
    private double current;
    private double power;

    public CircuitWire(CircuitNode startNode, CircuitNode endNode, double resistance, double emf) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.resistance = resistance;
        this.emf = emf;
    }

    public void setResistance(double resistance) {
        this.resistance = resistance;
    }

    public void setEmf(double emf) {
        this.emf = emf;
    }

    public void setStats(double voltage, double current, double power) {
        this.voltage = voltage;
        this.current = current;
        this.power = power;
    }

    public CircuitNode getOtherNode(CircuitNode node) {
        if(node == this.startNode) {
            return this.endNode;
        } else {
            return this.startNode;
        }
    }

    public CircuitNode getStartNode() {
        return startNode;
    }

    public CircuitNode getEndNode() {
        return endNode;
    }

    public double getResistance() {
        return resistance;
    }

    public double getEmf() {
        return emf;
    }

    public double getVoltage() {
        return voltage;
    }

    public double getCurrent() {
        return current;
    }

    public double getPower() {
        return power;
    }
}
