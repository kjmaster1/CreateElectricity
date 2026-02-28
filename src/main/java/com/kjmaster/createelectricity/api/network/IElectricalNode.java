package com.kjmaster.createelectricity.api.network;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Interface for any BlockEntity that acts as a vertex/node in the Electrodynamic Graph.
 */
public interface IElectricalNode {

    /**
     * Gets the unique ID of the network this node is currently part of.
     * @return The network ID, or null if it is isolated/disconnected.
     */
    @Nullable
    Long getNetworkId();

    /**
     * Sets the network ID. Called exclusively by the GridManager when
     * merging or splitting networks.
     * * @param networkId The ID of the parent network.
     */
    void setNetworkId(@Nullable Long networkId);

    /**
     * Gets the absolute world position of this node.
     * @return The BlockPos of the BlockEntity.
     */
    BlockPos getBlockPosition();

    /**
     * Gets all physical wires currently attached to this node.
     * @return A Set of WireConnections.
     */
    Set<WireConnection> getConnections();

    /**
     * Adds a new wire connection to this node.
     * This should trigger a block update so the client can render the new wire.
     * * @param connection The wire being attached.
     */
    void addConnection(WireConnection connection);

    /**
     * Removes a wire connection from this node (e.g., if it was cut with wire cutters or melted).
     * * @param connection The wire being removed.
     */
    void removeConnection(WireConnection connection);

    /**
     * Maximum amount of wires that can physically be attached to this node.
     * E.g., a simple insulator might only support 2, while a large pylon might support 8.
     */
    default int getMaxConnections() {
        return 4; // Sensible default
    }
}