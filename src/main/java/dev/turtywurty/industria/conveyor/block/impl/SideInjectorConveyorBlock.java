package dev.turtywurty.industria.conveyor.block.impl;

import dev.turtywurty.industria.conveyor.block.BaseConveyorBlock;
import dev.turtywurty.industria.conveyor.block.ConveyorConnectionType;
import dev.turtywurty.industria.conveyor.block.ConveyorInput;
import dev.turtywurty.industria.conveyor.block.ConveyorOutput;
import dev.turtywurty.industria.conveyor.block.ConveyorTopology;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SideInjectorConveyorBlock extends AbstractPoweredConveyorBlock {
    public SideInjectorConveyorBlock(Properties settings) {
        super(settings, new BlockProperties());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferredFacing = context.getHorizontalDirection();

        for (Direction facing : getPlacementOrder(preferredFacing)) {
            BlockState candidateState = applyPlacementState(defaultBlockState(), context, facing);

            if (isValidInjectorPlacement(context.getLevel(), context.getClickedPos(), candidateState))
                return candidateState;
        }

        return null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isValidInjectorPlacement(level, pos, state);
    }

    @Override
    public ConveyorTopology getTopology(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction back = facing.getOpposite();

        return new ConveyorTopology(
                List.of(new ConveyorInput("in", pos.relative(back))),
                List.of(new ConveyorOutput("out", pos.relative(facing), pos))
        );
    }

    @Override
    public ConveyorConnectionType getConnectionType(Level level, BlockPos pos, BlockState state, ConveyorOutput output) {
        return ConveyorConnectionType.SIDE_INJECT;
    }

    @Override
    public boolean canConnectToConveyor(Level level, BlockPos pos, BlockState state, ConveyorOutput output, BlockPos targetPos, BlockState targetState) {
        return targetPos.equals(output.deliveryPos()) && super.canConnectToConveyor(level, pos, state, output, targetPos, targetState);
    }

    private static Direction[] getPlacementOrder(Direction preferredFacing) {
        return new Direction[] {
                preferredFacing,
                preferredFacing.getClockWise(),
                preferredFacing.getCounterClockWise(),
                preferredFacing.getOpposite()
        };
    }

    private static boolean isValidInjectorPlacement(LevelReader level, BlockPos pos, BlockState state) {
        if (!(state.getBlock() instanceof SideInjectorConveyorBlock sideInjector))
            return false;

        if (!(level instanceof Level actualLevel))
            return false;

        Direction facing = state.getValue(FACING);
        BlockPos targetPos = pos.relative(facing);
        BlockState targetState = level.getBlockState(targetPos);
        ConveyorTopology topology = sideInjector.getTopology(actualLevel, pos, state);
        if (topology.outputs().isEmpty())
            return false;

        ConveyorOutput output = topology.outputs().getFirst();
        return sideInjector.canConnectToConveyor(actualLevel, pos, state, output, targetPos, targetState);
    }
}
