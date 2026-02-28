package com.kjmaster.createelectricity.api.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The overarching manager for all electrical grids in a specific dimension.
 * Handles the graph mathematics: adding/removing wires, merging grids, and splitting grids.
 */
public class ElectricalGridManager extends SavedData {

    private static final String DATA_NAME = "create_electricity_grid";

    private final Map<Long, ElectrodynamicNetwork> networks = new HashMap<>();
    private final Map<BlockPos, IElectricalNode> loadedNodes = new HashMap<>();
    private long nextNetworkId = 1;

    // --- Graph Mutation Logic ---

    /**
     * Called when a player strings a wire between two blocks.
     */
    public void addWireConnection(WireConnection wire, IElectricalNode nodeA, IElectricalNode nodeB) {
        nodeA.addConnection(wire);
        nodeB.addConnection(wire);
        loadedNodes.put(nodeA.getBlockPosition(), nodeA);
        loadedNodes.put(nodeB.getBlockPosition(), nodeB);

        Long idA = nodeA.getNetworkId();
        Long idB = nodeB.getNetworkId();

        if (idA == null && idB == null) {
            // Neither has a network. Create a new one.
            ElectrodynamicNetwork newNet = createNetwork();
            newNet.addNode(nodeA);
            newNet.addNode(nodeB);
            newNet.addWire(wire);
        } else if (idA != null && idB == null) {
            // Add B to A's network
            networks.get(idA).addNode(nodeB);
            networks.get(idA).addWire(wire);
        } else if (idA == null) {
            // Add A to B's network
            networks.get(idB).addNode(nodeA);
            networks.get(idB).addWire(wire);
        } else if (!idA.equals(idB)) {
            // Both have networks, but they are different. We must MERGE them.
            mergeNetworks(idA, idB, wire);
        } else {
            // They are already in the same network, just adding a redundant loop wire.
            networks.get(idA).addWire(wire);
        }

        setDirty(); // Tell Minecraft to save this data
    }

    /**
     * Called when a wire is cut or destroyed.
     * This requires running a Breadth-First Search to see if the network split in two.
     */
    public void removeWireConnection(WireConnection wire) {
        IElectricalNode nodeA = loadedNodes.get(wire.nodeA());
        IElectricalNode nodeB = loadedNodes.get(wire.nodeB());

        if (nodeA != null) nodeA.removeConnection(wire);
        if (nodeB != null) nodeB.removeConnection(wire);

        if (nodeA == null || nodeA.getNetworkId() == null) return;

        ElectrodynamicNetwork net = networks.get(nodeA.getNetworkId());
        net.removeWire(wire);

        // BFS to check if nodeA can still reach nodeB. If not, the network has fractured.
        if (nodeB != null && !canReach(nodeA, nodeB)) {
            splitNetwork(net, nodeA, nodeB);
        }

        setDirty();
    }

    // --- Internal Graph Algorithms ---

    private void mergeNetworks(long idA, long idB, WireConnection bridgingWire) {
        ElectrodynamicNetwork netA = networks.get(idA);
        ElectrodynamicNetwork netB = networks.remove(idB); // Remove B from active networks

        // Move all nodes and wires from B into A
        for (IElectricalNode node : netB.getNodes()) {
            netA.addNode(node);
        }
        for (WireConnection wire : netB.getWires()) {
            netA.addWire(wire);
        }
        netA.addWire(bridgingWire);
    }

    /**
     * Breadth-First Search to determine if a path exists between start and target.
     */
    private boolean canReach(IElectricalNode start, IElectricalNode target) {
        Set<BlockPos> visited = new HashSet<>();
        Queue<IElectricalNode> queue = new LinkedList<>();

        queue.add(start);
        visited.add(start.getBlockPosition());

        while (!queue.isEmpty()) {
            IElectricalNode current = queue.poll();
            if (current.getBlockPosition().equals(target.getBlockPosition())) {
                return true;
            }

            for (WireConnection wire : current.getConnections()) {
                BlockPos neighborPos = wire.getOppositeNode(current.getBlockPosition());
                if (neighborPos != null && !visited.contains(neighborPos)) {
                    visited.add(neighborPos);
                    IElectricalNode neighbor = loadedNodes.get(neighborPos);
                    if (neighbor != null) queue.add(neighbor);
                }
            }
        }
        return false;
    }

    /**
     * Rebuilds the fractured network starting from nodeB into a brand-new network.
     */
    private void splitNetwork(ElectrodynamicNetwork oldNet, IElectricalNode nodeA, IElectricalNode nodeB) {
        ElectrodynamicNetwork newNet = createNetwork();

        Set<BlockPos> visited = new HashSet<>();
        Queue<IElectricalNode> queue = new LinkedList<>();

        queue.add(nodeB);
        visited.add(nodeB.getBlockPosition());

        // Traverse everything connected to B
        while (!queue.isEmpty()) {
            IElectricalNode current = queue.poll();

            // Move node to new network
            oldNet.removeNode(current);
            newNet.addNode(current);

            for (WireConnection wire : current.getConnections()) {
                // Move wire to new network
                oldNet.removeWire(wire);
                newNet.addWire(wire);

                BlockPos neighborPos = wire.getOppositeNode(current.getBlockPosition());
                if (neighborPos != null && !visited.contains(neighborPos)) {
                    visited.add(neighborPos);
                    IElectricalNode neighbor = loadedNodes.get(neighborPos);
                    if (neighbor != null) queue.add(neighbor);
                }
            }
        }
    }

    // --- Lifecycle and Saving ---

    private ElectrodynamicNetwork createNetwork() {
        ElectrodynamicNetwork net = new ElectrodynamicNetwork(nextNetworkId++);
        networks.put(net.getId(), net);
        return net;
    }

    public void tick() {
        for (ElectrodynamicNetwork network : networks.values()) {
            network.tickNetwork();
        }
    }

    // NeoForge 1.21.1 NBT Serialization for SavedData
    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        tag.putLong("NextNetworkId", nextNetworkId);
        // In a full implementation, you serialize all the WireConnections to NBT lists here
        // so they persist through server restarts!
        return tag;
    }

    public static ElectricalGridManager load(CompoundTag tag, HolderLookup.Provider registries) {
        ElectricalGridManager manager = new ElectricalGridManager();
        manager.nextNetworkId = tag.getLong("NextNetworkId");
        // Deserialize wire lists here
        return manager;
    }

    /**
     * How to retrieve this manager from the level.
     */
    public static ElectricalGridManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        ElectricalGridManager::new,
                        ElectricalGridManager::load
                ),
                DATA_NAME
        );
    }
}