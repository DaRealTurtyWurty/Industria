package dev.turtywurty.industria.conveyor.block;

import dev.turtywurty.industria.conveyor.ConveyorItem;
import dev.turtywurty.industria.conveyor.ConveyorNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ConveyorLike {
    int getItemLimit(BlockGetter level, BlockPos pos, BlockState state);

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

    default boolean canAttachToStorageOutput(Level level, BlockPos pos, BlockState state, ConveyorOutput output, BlockPos storagePos) {
        return false;
    }

    default boolean canOutputToStorage(Level level, BlockPos pos, BlockState state, ConveyorItem item, ConveyorOutput output,
                                       BlockPos storagePos, ConveyorNetwork network, ConveyorRoutingState routingState) {
        return canAttachToStorageOutput(level, pos, state, output, storagePos);
    }

    default void onOutputUsed(Level level, BlockPos pos, BlockState state, ConveyorOutput output, ConveyorRoutingState routingState) {
        // NO-OP
    }

    default ConveyorConnectionType getConnectionType(Level level, BlockPos pos, BlockState state, ConveyorOutput output) {
        return ConveyorConnectionType.STANDARD;
    }

    default boolean canConnectToConveyor(Level level, BlockPos pos, BlockState state, ConveyorOutput output, BlockPos targetPos, BlockState targetState) {
        if (!(targetState.getBlock() instanceof ConveyorLike targetConveyor))
            return false;

        return targetConveyor.canAcceptIncomingConnection(
                level,
                targetPos,
                targetState,
                pos,
                state,
                output,
                getConnectionType(level, pos, state, output));
    }

    default boolean canAcceptIncomingConnection(Level level, BlockPos pos, BlockState state, BlockPos sourcePos, BlockState sourceState,
                                                ConveyorOutput sourceOutput, ConveyorConnectionType connectionType) {
        return connectionType == ConveyorConnectionType.STANDARD
                && getTopology(level, pos, state).acceptsInputFrom(sourceOutput.expectedInputPos());
    }
}
