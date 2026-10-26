package dev.turtywurty.industria.connected;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public record ConnectedTextureFace(Direction up, Direction right) {
    public ConnectedTextureFace {
        Objects.requireNonNull(up, "Up direction cannot be null");
        Objects.requireNonNull(right, "Right direction cannot be null");

        if (up == right || up == right.getOpposite())
            throw new IllegalArgumentException("Up and right directions must be perpendicular");
    }

    public int getMask(BlockGetter level, BlockPos pos, BlockState state) {
        boolean topLeft = isConnected(level, pos, state, this.up, this.right.getOpposite());
        boolean top = isConnected(level, pos, state, this.up);
        boolean topRight = isConnected(level, pos, state, this.up, this.right);
        boolean left = isConnected(level, pos, state, this.right.getOpposite());
        boolean right = isConnected(level, pos, state, this.right);
        boolean bottomLeft = isConnected(level, pos, state, this.up.getOpposite(), this.right.getOpposite());
        boolean bottom = isConnected(level, pos, state, this.up.getOpposite());
        boolean bottomRight = isConnected(level, pos, state, this.up.getOpposite(), this.right);

        return ConnectedTextureMask.fromConnections(
                topLeft, top, topRight,
                left, right,
                bottomLeft, bottom, bottomRight
        );
    }

    private boolean isConnected(BlockGetter level, BlockPos pos, BlockState state, Direction... directions) {
        BlockPos offsetPos = pos;
        for (Direction direction : directions) {
            offsetPos = offsetPos.relative(direction);
        }
        BlockState offsetState = level.getBlockState(offsetPos);
        return state.is(offsetState.getBlock());
    }


    public static ConnectedTextureFace fromFace(Direction face) {
        return switch (face) {
            case NORTH -> new ConnectedTextureFace(Direction.UP, Direction.WEST);
            case SOUTH -> new ConnectedTextureFace(Direction.UP, Direction.EAST);
            case WEST -> new ConnectedTextureFace(Direction.UP, Direction.SOUTH);
            case EAST -> new ConnectedTextureFace(Direction.UP, Direction.NORTH);
            case UP -> new ConnectedTextureFace(Direction.NORTH, Direction.EAST);
            case DOWN -> new ConnectedTextureFace(Direction.SOUTH, Direction.EAST);
        };
    }
}
