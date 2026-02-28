package com.kjmaster.createelectricity.content.infrastructure;

import com.kjmaster.createelectricity.api.network.ElectricalGridManager;
import com.kjmaster.createelectricity.api.network.IElectricalNode;
import com.kjmaster.createelectricity.api.network.WireConnection;
import com.kjmaster.createelectricity.api.network.WireType;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CeramicInsulatorBlockEntity extends SmartBlockEntity implements IElectricalNode {

    private Long networkId = null;
    private final Set<WireConnection> connections = new HashSet<>();

    public CeramicInsulatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        // No specific Create behaviours needed for a simple relay yet
    }

    // --- IElectricalNode Implementation ---

    @Override
    public @Nullable Long getNetworkId() {
        return this.networkId;
    }

    @Override
    public void setNetworkId(@Nullable Long networkId) {
        this.networkId = networkId;
    }

    @Override
    public BlockPos getBlockPosition() {
        return this.worldPosition;
    }

    @Override
    public Set<WireConnection> getConnections() {
        return this.connections;
    }

    @Override
    public void addConnection(WireConnection connection) {
        this.connections.add(connection);
        notifyUpdate(); // Tells the client to re-render the wires
    }

    @Override
    public void removeConnection(WireConnection connection) {
        this.connections.remove(connection);
        notifyUpdate();
    }

    @Override
    public int getMaxConnections() {
        return 6; // Relays can handle a decent number of outgoing lines
    }

    // --- Lifecycle ---

    @Override
    public void destroy() {
        super.destroy();
        // If the block is broken, we must cut all attached wires in the Grid Manager!
        if (level instanceof ServerLevel serverLevel && !connections.isEmpty()) {
            ElectricalGridManager manager = ElectricalGridManager.get(serverLevel);
            // Copy the set to avoid ConcurrentModificationException while removing
            Set<WireConnection> toRemove = new HashSet<>(connections);
            for (WireConnection wire : toRemove) {
                manager.removeWireConnection(wire);
            }
        }
    }

    // --- Serialization (Saving the Wires) ---

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        if (this.networkId != null) {
            tag.putLong("NetworkId", this.networkId);
        }

        ListTag connectionsList = new ListTag();
        for (WireConnection wire : connections) {
            CompoundTag wireTag = new CompoundTag();

            // We only need to save the "other" node's position and the wire type
            BlockPos oppositePos = wire.getOppositeNode(this.worldPosition);
            if (oppositePos != null) {
                wireTag.put("TargetPos", NbtUtils.writeBlockPos(oppositePos));
                wireTag.putString("WireType", wire.wireType().name());
                connectionsList.add(wireTag);
            }
        }
        tag.put("WireConnections", connectionsList);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        if (tag.contains("NetworkId")) {
            this.networkId = tag.getLong("NetworkId");
        } else {
            this.networkId = null;
        }

        this.connections.clear();
        if (tag.contains("WireConnections")) {
            ListTag connectionsList = tag.getList("WireConnections", Tag.TAG_COMPOUND);
            for (int i = 0; i < connectionsList.size(); i++) {
                CompoundTag wireTag = connectionsList.getCompound(i);

                BlockPos targetPos = NbtUtils.readBlockPos(wireTag, "TargetPos").orElse(null);
                if (targetPos != null) {
                    try {
                        WireType type = WireType.valueOf(wireTag.getString("WireType"));
                        // Reconstruct the immutable record
                        this.connections.add(new WireConnection(this.worldPosition, targetPos, type));
                    } catch (IllegalArgumentException e) {
                        // Unknown wire type (perhaps a removed addon), skip it.
                    }
                }
            }
        }
    }
}