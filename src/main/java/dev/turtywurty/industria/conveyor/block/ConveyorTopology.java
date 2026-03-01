package dev.turtywurty.industria.conveyor.block;

import net.minecraft.core.BlockPos;

import java.util.List;

public record ConveyorTopology(
        List<ConveyorInput> inputs,
        List<ConveyorOutput> outputs
) {
    public boolean acceptsInputFrom(BlockPos sourcePos) {
        return inputs.stream().anyMatch(input -> input.expectedSourcePos().equals(sourcePos));
    }
}
