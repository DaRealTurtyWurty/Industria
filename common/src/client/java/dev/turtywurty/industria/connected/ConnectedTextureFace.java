package dev.turtywurty.industria.connected;

import net.minecraft.core.Direction;

import java.util.Objects;

public record ConnectedTextureFace(Direction up, Direction right) {
    private static final ConnectedTextureFace NORTH_FACE = new ConnectedTextureFace(Direction.UP, Direction.WEST);
    private static final ConnectedTextureFace SOUTH_FACE = new ConnectedTextureFace(Direction.UP, Direction.EAST);
    private static final ConnectedTextureFace WEST_FACE = new ConnectedTextureFace(Direction.UP, Direction.SOUTH);
    private static final ConnectedTextureFace EAST_FACE = new ConnectedTextureFace(Direction.UP, Direction.NORTH);
    private static final ConnectedTextureFace UP_FACE = new ConnectedTextureFace(Direction.NORTH, Direction.EAST);
    private static final ConnectedTextureFace DOWN_FACE = new ConnectedTextureFace(Direction.SOUTH, Direction.EAST);

    public ConnectedTextureFace {
        Objects.requireNonNull(up, "Up direction cannot be null");
        Objects.requireNonNull(right, "Right direction cannot be null");

        if (up == right || up == right.getOpposite())
            throw new IllegalArgumentException("Up and right directions must be perpendicular");
    }

    public static ConnectedTextureFace fromFace(Direction face) {
        return switch (face) {
            case NORTH -> NORTH_FACE;
            case SOUTH -> SOUTH_FACE;
            case WEST -> WEST_FACE;
            case EAST -> EAST_FACE;
            case UP -> UP_FACE;
            case DOWN -> DOWN_FACE;
        };
    }

    public int getMask(ConnectedTextureNeighborhood neighborhood, ConnectedTextureAxis axis) {
        Direction leftDirection = this.right.getOpposite();
        Direction downDirection = this.up.getOpposite();
        boolean top = axis.allowsVertical() && neighborhood.isConnected(this.up);
        boolean bottom = axis.allowsVertical() && neighborhood.isConnected(downDirection);
        boolean left = axis.allowsHorizontal() && neighborhood.isConnected(leftDirection);
        boolean right = axis.allowsHorizontal() && neighborhood.isConnected(this.right);
        boolean topLeft = top && left && neighborhood.isConnected(this.up, leftDirection);
        boolean topRight = top && right && neighborhood.isConnected(this.up, this.right);
        boolean bottomLeft = bottom && left && neighborhood.isConnected(downDirection, leftDirection);
        boolean bottomRight = bottom && right && neighborhood.isConnected(downDirection, this.right);

        return ConnectedTextureMask.fromConnections(
                topLeft, top, topRight,
                left, right,
                bottomLeft, bottom, bottomRight
        );
    }
}
