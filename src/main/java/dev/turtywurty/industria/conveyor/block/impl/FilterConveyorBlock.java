package dev.turtywurty.industria.conveyor.block.impl;

import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import dev.turtywurty.industria.conveyor.block.*;
import dev.turtywurty.industria.conveyor.block.impl.entity.FilterConveyorBlockEntity;
import dev.turtywurty.industria.init.BlockEntityTypeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class FilterConveyorBlock extends AbstractPoweredConveyorBlock {
    public static final String FORWARD_OUTPUT_ID = "forward";
    public static final String RIGHT_OUTPUT_ID = "right";

    public FilterConveyorBlock(Properties settings) {
        super(settings, new BlockProperties()
                .blockEntityProperties(new BlockProperties.BlockBlockEntityProperties<>(() -> BlockEntityTypeInit.FILTER_CONVEYOR)
                        .rightClickToOpenGui())
        );
    }

    @Override
    public ConveyorTopology getTopology(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        Direction back = facing.getOpposite();
        Direction right = facing.getClockWise();

        return new ConveyorTopology(
                List.of(new ConveyorInput("in", pos.relative(back))),
                List.of(
                        new ConveyorOutput(FORWARD_OUTPUT_ID, pos.relative(facing), pos),
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
                if (output.id().equals(selectedOutputId))
                    return output;
            }
        }

        if (level.getBlockEntity(pos) instanceof FilterConveyorBlockEntity blockEntity) {
            ConveyorOutput conveyorOutput = blockEntity.doesMatchFilter(item.getStack()) ?
                    topology.outputs().stream()
                            .filter(output -> output.id().equals(FORWARD_OUTPUT_ID))
                            .findFirst()
                            .orElse(null) :
                    topology.outputs().stream()
                            .filter(output -> output.id().equals(RIGHT_OUTPUT_ID))
                            .findFirst()
                            .orElse(null);
            item.setSelectedOutputId(conveyorOutput != null ? conveyorOutput.id() : null);
            return conveyorOutput;
        }

        return null;
    }
}
