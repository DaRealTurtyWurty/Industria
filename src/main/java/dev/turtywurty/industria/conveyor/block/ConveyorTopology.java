package dev.turtywurty.industria.conveyor.block;

import net.minecraft.core.BlockPos;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record ConveyorTopology(
        List<ConveyorInput> inputs,
        List<ConveyorOutput> outputs
) {
    @Nullable
    public ConveyorInput getInputFrom(BlockPos sourcePos) {
        return inputs.stream()
                .filter(input -> input.expectedSourcePos().equals(sourcePos))
                .findFirst()
                .orElse(null);
    }

    public boolean acceptsInputFrom(BlockPos sourcePos) {
        return getInputFrom(sourcePos) != null;
    }
}
