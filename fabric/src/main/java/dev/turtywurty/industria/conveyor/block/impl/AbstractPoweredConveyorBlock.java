package dev.turtywurty.industria.conveyor.block.impl;

import dev.turtywurty.industria.block.abstraction.IndustriaBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

public abstract class AbstractPoweredConveyorBlock extends AbstractHorizontalConveyorBlock {
    public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");

    protected AbstractPoweredConveyorBlock(Properties settings, IndustriaBlock.BlockProperties properties) {
        super(settings, properties
                .addStateProperty(ENABLED, true));
    }

    protected BlockState applyPlacementState(BlockState state, BlockPlaceContext context, Direction facing) {
        return super.applyPlacementState(state, context, facing)
                .setValue(ENABLED, !context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);

        if (level.isClientSide())
            return;

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.scheduleTick(pos, this, 1);
        }
    }

    protected BlockState resolveState(ServerLevel level, BlockPos pos, BlockState current) {
        boolean enabled = !level.hasNeighborSignal(pos);
        return super.resolveState(level, pos, current)
                .setValue(ENABLED, enabled);
    }

    @Override
    public int getSpeed(Level level, BlockPos pos, BlockState state) {
        return isEnabled(level, pos, state) ? getEnabledSpeed(level, pos, state) : 0;
    }

    protected int getEnabledSpeed(Level level, BlockPos pos, BlockState state) {
        return 5;
    }

    @Override
    public boolean isEnabled(Level level, BlockPos pos, BlockState state) {
        return state.getValue(ENABLED);
    }
}
