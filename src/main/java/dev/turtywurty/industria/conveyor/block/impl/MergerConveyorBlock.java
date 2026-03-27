package dev.turtywurty.industria.conveyor.block.impl;

import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import dev.turtywurty.industria.conveyor.block.BaseConveyorBlock;
import dev.turtywurty.industria.conveyor.block.ConveyorInput;
import dev.turtywurty.industria.conveyor.block.ConveyorOutput;
import dev.turtywurty.industria.conveyor.block.ConveyorRoutingState;
import dev.turtywurty.industria.conveyor.block.ConveyorTopology;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class MergerConveyorBlock extends AbstractPoweredConveyorBlock {
    public static final String LEFT_INPUT_ID = "left";
    public static final String RIGHT_INPUT_ID = "right";

    public MergerConveyorBlock(Properties settings) {
        super(settings, new BlockProperties());
    }

    @Override
    public ConveyorTopology getTopology(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();

        return new ConveyorTopology(
                List.of(
                        new ConveyorInput(LEFT_INPUT_ID, pos.relative(left)),
                        new ConveyorInput(RIGHT_INPUT_ID, pos.relative(right))
                ),
                List.of(new ConveyorOutput("out", pos.relative(facing), pos))
        );
    }

    @Override
    public boolean canAcceptIncomingItem(Level level, BlockPos pos, BlockState state, ConveyorItem item, BlockPos inputPos,
                                         ConveyorNetwork network, ConveyorRoutingState routingState) {
        ConveyorTopology topology = getTopology(level, pos, state);
        if (topology.inputs().size() <= 1)
            return true;

        int inputIndex = getInputIndex(topology, inputPos);
        if (inputIndex < 0)
            return true;

        int preferredInputIndex = routingState.getRoundRobinIndex(pos, topology.inputs().size());
        if (inputIndex == preferredInputIndex)
            return true;

        BlockPos preferredInputPos = topology.inputs().get(preferredInputIndex).expectedSourcePos();
        return !network.hasReadyItemForInput(level, pos, preferredInputPos);
    }

    @Override
    public void onIncomingItemAccepted(Level level, BlockPos pos, BlockState state, ConveyorItem item, BlockPos inputPos,
                                       ConveyorNetwork network, ConveyorRoutingState routingState) {
        ConveyorTopology topology = getTopology(level, pos, state);
        if (topology.inputs().size() <= 1)
            return;

        int inputIndex = getInputIndex(topology, inputPos);
        if (inputIndex < 0)
            return;

        routingState.setRoundRobinIndex(pos, inputIndex + 1, topology.inputs().size());
    }

    private static int getInputIndex(ConveyorTopology topology, BlockPos inputPos) {
        for (int index = 0; index < topology.inputs().size(); index++) {
            if (topology.inputs().get(index).expectedSourcePos().equals(inputPos))
                return index;
        }

        return -1;
    }
}
