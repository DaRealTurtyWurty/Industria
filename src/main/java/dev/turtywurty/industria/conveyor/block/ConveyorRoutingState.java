package dev.turtywurty.industria.conveyor.block;

import net.minecraft.core.BlockPos;

public interface ConveyorRoutingState {
    int getRoundRobinIndex(BlockPos pos, int outputCount);

    void advanceRoundRobinIndex(BlockPos pos, int outputCount);

    void setRoundRobinIndex(BlockPos pos, int index, int outputCount);
}
