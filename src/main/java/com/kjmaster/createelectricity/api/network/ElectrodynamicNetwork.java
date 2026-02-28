package com.kjmaster.createelectricity.api.network;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a single, isolated electrical grid.
 */
public class ElectrodynamicNetwork {

    private final long id;
    private final Set<IElectricalNode> nodes;
    private final Set<WireConnection> wires;

    // Cached physics values so we don't recalculate them every tick
    private float totalResistance;

    public ElectrodynamicNetwork(long id) {
        this.id = id;
        this.nodes = new HashSet<>();
        this.wires = new HashSet<>();
        this.totalResistance = 0.0f;
    }

    public long getId() {
        return id;
    }

    public Set<IElectricalNode> getNodes() {
        return nodes;
    }

    public Set<WireConnection> getWires() {
        return wires;
    }

    public void addNode(IElectricalNode node) {
        if (this.nodes.add(node)) {
            node.setNetworkId(this.id);
        }
    }

    public void removeNode(IElectricalNode node) {
        if (this.nodes.remove(node)) {
            node.setNetworkId(null);
        }
    }

    public void addWire(WireConnection wire) {
        if (this.wires.add(wire)) {
            this.totalResistance += wire.getTotalResistance();
        }
    }

    public void removeWire(WireConnection wire) {
        if (this.wires.remove(wire)) {
            this.totalResistance -= wire.getTotalResistance();
            if (this.totalResistance < 0) this.totalResistance = 0; // Floating point safety
        }
    }

    /**
     * Called once per server tick by the Grid Manager.
     * This is where the actual $P = V \times A$ and Joule Heating math will live.
     */
    public void tickNetwork() {
        if (nodes.isEmpty()) return;

        // Step 1: Tally total FE available from generators on this grid.
        // Step 2: Tally total FE demanded by consumer machines.
        // Step 3: Check if requested Amperage > any wire's maxAmperage limit.
        // Step 4: Distribute power to nodes and subtract I^2*R loss.

    }
}