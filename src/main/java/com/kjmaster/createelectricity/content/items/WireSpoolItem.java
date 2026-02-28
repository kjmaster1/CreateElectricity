package com.kjmaster.createelectricity.content.items;

import com.kjmaster.createelectricity.AllDataComponents;
import com.kjmaster.createelectricity.api.network.ElectricalGridManager;
import com.kjmaster.createelectricity.api.network.IElectricalNode;
import com.kjmaster.createelectricity.api.network.WireConnection;
import com.kjmaster.createelectricity.api.network.WireType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class WireSpoolItem extends Item {

    private final WireType wireType;
    private final float maxWireLength;

    public WireSpoolItem(Properties properties, WireType wireType, float maxWireLength) {
        super(properties);
        this.wireType = wireType;
        this.maxWireLength = maxWireLength;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player == null) return InteractionResult.PASS;

        // Ensure we are clicking on an electrical node
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof IElectricalNode clickedNode)) {
            return InteractionResult.PASS;
        }

        // We only do the heavy logic and saving on the logical Server
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        GlobalPos storedPos = stack.get(AllDataComponents.SELECTED_NODE);

        // --- CASE 1: Spool is empty. Start a connection. ---
        if (storedPos == null) {
            stack.set(AllDataComponents.SELECTED_NODE, GlobalPos.of(level.dimension(), pos));

            player.displayClientMessage(Component.translatable("create_electricity.wire.attached_first", pos.toShortString()), true);
            level.playSound(null, pos, SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.BLOCKS, 1.0F, 1.2F);
            return InteractionResult.SUCCESS;
        }

        // --- CASE 2: Player clicked the exact same block. Cancel the connection. ---
        if (storedPos.dimension() == level.dimension() && storedPos.pos().equals(pos)) {
            stack.remove(AllDataComponents.SELECTED_NODE);

            player.displayClientMessage(Component.translatable("create_electricity.wire.cleared"), true);
            return InteractionResult.SUCCESS;
        }

        // --- CASE 3: Trying to connect across dimensions (Not allowed) ---
        if (storedPos.dimension() != level.dimension()) {
            player.displayClientMessage(Component.translatable("create_electricity.wire.wrong_dimension").withStyle(ChatFormatting.RED), true);
            stack.remove(AllDataComponents.SELECTED_NODE);
            return InteractionResult.FAIL;
        }

        // --- CASE 4: Validate the first node still exists ---
        BlockPos firstPos = storedPos.pos();
        BlockEntity firstBe = level.getBlockEntity(firstPos);
        if (!(firstBe instanceof IElectricalNode firstNode)) {
            player.displayClientMessage(Component.translatable("create_electricity.wire.missing_first").withStyle(ChatFormatting.RED), true);
            stack.remove(AllDataComponents.SELECTED_NODE);
            return InteractionResult.FAIL;
        }

        // --- CASE 5: Validate distance and physics ---
        WireConnection newConnection = new WireConnection(firstPos, pos, this.wireType);

        if (newConnection.getDistance() > this.maxWireLength) {
            player.displayClientMessage(Component.translatable("create_electricity.wire.too_far").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        // Check if nodes have open connection slots
        if (firstNode.getConnections().size() >= firstNode.getMaxConnections() ||
                clickedNode.getConnections().size() >= clickedNode.getMaxConnections()) {
            player.displayClientMessage(Component.translatable("create_electricity.wire.too_many_connections").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        // --- FINAL: Form the Connection ---
        ElectricalGridManager manager = ElectricalGridManager.get((ServerLevel) level);
        manager.addWireConnection(newConnection, firstNode, clickedNode);

        // Clear the data component and consume a wire from the stack
        stack.remove(AllDataComponents.SELECTED_NODE);
        if (!player.isCreative()) {
            stack.shrink(1);
        }

        level.playSound(null, pos, SoundEvents.TRIPWIRE_ATTACH, SoundSource.BLOCKS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("create_electricity.wire.connected").withStyle(ChatFormatting.GREEN), true);

        return InteractionResult.SUCCESS;
    }
}
