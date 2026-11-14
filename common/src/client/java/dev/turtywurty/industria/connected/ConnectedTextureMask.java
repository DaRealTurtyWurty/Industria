package dev.turtywurty.industria.connected;

public class ConnectedTextureMask {
    public static final int TOP_LEFT = 1;
    public static final int TOP = 2;
    public static final int TOP_RIGHT = 4;
    public static final int LEFT = 8;
    public static final int CENTER = 16;
    public static final int RIGHT = 32;
    public static final int BOTTOM_LEFT = 64;
    public static final int BOTTOM = 128;
    public static final int BOTTOM_RIGHT = 256;

    public static int fromConnections(
            boolean topLeft, boolean top, boolean topRight,
            boolean left, boolean right,
            boolean bottomLeft, boolean bottom, boolean bottomRight
    ) {
        int mask = CENTER;
        if (topLeft && top && left) mask |= TOP_LEFT;
        if (top) mask |= TOP;
        if (topRight && top && right) mask |= TOP_RIGHT;
        if (left) mask |= LEFT;
        if (right) mask |= RIGHT;
        if (bottomLeft && bottom && left) mask |= BOTTOM_LEFT;
        if (bottom) mask |= BOTTOM;
        if (bottomRight && bottom && right) mask |= BOTTOM_RIGHT;
        return mask;
    }
}
