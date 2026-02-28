package com.kjmaster.createelectricity.content.infrastructure;

import com.kjmaster.createelectricity.AllBlockEntityTypes;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;

public class CeramicInsulatorBlock extends DirectionalBlock implements IBE<CeramicInsulatorBlockEntity> {

    public static final MapCodec<CeramicInsulatorBlock> CODEC = simpleCodec(CeramicInsulatorBlock::new);

    public CeramicInsulatorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected @NotNull MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Place the insulator attached to the face the player clicked
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    // --- Block Entity Destruction Logic ---

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // When the block is broken, tell the block entity to snap all its wires
            BlockEntityHelper.destroy(level, pos);
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    // --- IBE Implementation ---

    @Override
    public Class<CeramicInsulatorBlockEntity> getBlockEntityClass() {
        return CeramicInsulatorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CeramicInsulatorBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.CERAMIC_INSULATOR.get();
    }

    // Helper to safely trigger destroy on our BE
    private static class BlockEntityHelper {
        static void destroy(Level level, BlockPos pos) {
            if (level.getBlockEntity(pos) instanceof CeramicInsulatorBlockEntity be) {
                be.destroy();
            }
        }
    }
}