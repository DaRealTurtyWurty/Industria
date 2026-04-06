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

public class SplitterConveyorBlock extends AbstractPoweredConveyorBlock {
    public static final String LEFT_OUTPUT_ID = "left";
    public static final String RIGHT_OUTPUT_ID = "right";

    public SplitterConveyorBlock(Properties settings) {
        super(settings, new BlockProperties());
    }

    @Override
    public ConveyorTopology getTopology(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction back = facing.getOpposite();
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();

        return new ConveyorTopology(
                List.of(new ConveyorInput("in", pos.relative(back))),
                List.of(
                        new ConveyorOutput(LEFT_OUTPUT_ID, pos.relative(left), pos),
                        new ConveyorOutput(RIGHT_OUTPUT_ID, pos.relative(right), pos)
                )
        );
    }

    @Override
    public ConveyorOutput selectOutput(Level level, BlockPos pos, BlockState state, ConveyorItem item, ConveyorNetwork network, ConveyorRoutingState routingState) {
        ConveyorTopology topology = getTopology(level, pos, state);
        if (topology.outputs().isEmpty())
            return null;

        String selectedOutputId = item.getSelectedOutputId();
        if (selectedOutputId != null) {
            for (ConveyorOutput output : topology.outputs()) {
                if (output.id().equals(selectedOutputId)) {
                    return output;
                }
            }
        }

        int outputIndex = routingState.getRoundRobinIndex(pos, topology.outputs().size());
        ConveyorOutput output = topology.outputs().get(outputIndex);
        item.setSelectedOutputId(output.id());
        routingState.advanceRoundRobinIndex(pos, topology.outputs().size());
        return output;
    }
}
