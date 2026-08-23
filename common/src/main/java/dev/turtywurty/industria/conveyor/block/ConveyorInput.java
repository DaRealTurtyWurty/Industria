package dev.turtywurty.industria.conveyor.block;

import net.minecraft.core.BlockPos;

public record ConveyorInput(
        String id,
        BlockPos expectedSourcePos
) {
}
