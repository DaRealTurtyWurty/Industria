package dev.turtywurty.industria.conveyor.block;

import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ConveyorLike {
    int getItemLimit(Level level, BlockPos pos, BlockState state);

    int getSpeed(Level level, BlockPos pos, BlockState state);

    default boolean isEnabled(Level level, BlockPos pos, BlockState state) {
        return true;
    }

    ConveyorTopology getTopology(Level level, BlockPos pos, BlockState state);

    default ConveyorOutput selectOutput(Level level, BlockPos pos, BlockState state, ConveyorItem item, ConveyorNetwork network, ConveyorRoutingState routingState) {
        return getTopology(level, pos, state).outputs().getFirst();
    }

    default boolean canAcceptIncomingItem(Level level, BlockPos pos, BlockState state, ConveyorItem item, BlockPos inputPos,
                                          ConveyorNetwork network, ConveyorRoutingState routingState) {
        return true;
    }

    default void onIncomingItemAccepted(Level level, BlockPos pos, BlockState state, ConveyorItem item, BlockPos inputPos,
                                        ConveyorNetwork network, ConveyorRoutingState routingState) {
        // NO-OP
    }

    default void onOutputUsed(Level level, BlockPos pos, BlockState state, ConveyorOutput output, ConveyorRoutingState routingState) {
        // NO-OP
    }
}
