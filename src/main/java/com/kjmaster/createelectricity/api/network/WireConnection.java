package com.kjmaster.createelectricity.api.network;

import net.minecraft.core.BlockPos;

/**
 * Represents a physical wire hanging between two nodes in the world.
 */
public record WireConnection(BlockPos nodeA, BlockPos nodeB, WireType wireType) {

    /**
     * Calculates the physical Euclidean distance between the two nodes.
     * @return The distance in meters (blocks).
     */
    public float getDistance() {
        return (float) Math.sqrt(nodeA.distSqr(nodeB));
    }

    /**
     * Calculates the total electrical resistance of this specific hanging wire.
     * R = Length * ResistancePerMeter
     * * @return The resistance in Ohms.
     */
    public float getTotalResistance() {
        return getDistance() * wireType.getResistancePerMeter();
    }

    /**
     * Helper method for graph traversal.
     * @param pos The position of the current node.
     * @return True if this wire is attached to the given position.
     */
    public boolean connectsTo(BlockPos pos) {
        return nodeA.equals(pos) || nodeB.equals(pos);
    }

    /**
     * Given one end of the wire, returns the position of the other end.
     * Useful for BFS/DFS pathfinding in the Grid Manager.
     * * @param from The node you are currently looking at.
     * @return The node on the other end, or null if 'from' is not part of this wire.
     */
    public BlockPos getOppositeNode(BlockPos from) {
        if (nodeA.equals(from)) return nodeB;
        if (nodeB.equals(from)) return nodeA;
        return null;
    }

    /**
     * Standardizes the connection so A->B is evaluated the same as B->A.
     * This is crucial for preventing duplicate wires in HashSets.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WireConnection(BlockPos a, BlockPos b, WireType type))) return false;

        boolean typeMatches = this.wireType == type;
        boolean exactMatch = this.nodeA.equals(a) && this.nodeB.equals(b);
        boolean flippedMatch = this.nodeA.equals(b) && this.nodeB.equals(a);

        return typeMatches && (exactMatch || flippedMatch);
    }

    @Override
    public int hashCode() {
        // Commutative hash code so A->B and B->A produce the same hash
        return nodeA.hashCode() + nodeB.hashCode() + wireType.hashCode();
    }
}