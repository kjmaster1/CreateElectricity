package com.kjmaster.createelectricity.content.kinetics.dynamo;

import com.kjmaster.createelectricity.AllBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The early-game Magnetic Dynamo.
 * Converts rotational kinetic energy (RPM) into early-tier Electrodynamic Power.
 */
public class MagneticDynamoBlock extends DirectionalKineticBlock implements IBE<MagneticDynamoBlockEntity> {

    public MagneticDynamoBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        // The shaft connects to the facing direction of the block
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        // Only allow shaft connections on the exact face the dynamo is pointing
        return face == state.getValue(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = context.getClickedFace();
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            preferred = preferred.getOpposite();
        }
        return this.defaultBlockState().setValue(FACING, preferred);
    }

    @Override
    public Class<MagneticDynamoBlockEntity> getBlockEntityClass() {
        return MagneticDynamoBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends MagneticDynamoBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.MAGNETIC_DYNAMO.get();
    }
}
