package dev.turtywurty.industria.conveyor.block;

import net.minecraft.core.BlockPos;

public interface ConveyorRoutingState {
    int nextRoundRobinIndex(BlockPos pos, int outputCount);
}
