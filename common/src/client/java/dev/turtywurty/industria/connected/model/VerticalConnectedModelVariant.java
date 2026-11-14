package dev.turtywurty.industria.connected.model;

public enum VerticalConnectedModelVariant {
    NORMAL,
    LOWER,
    MIDDLE,
    UPPER;

    public static VerticalConnectedModelVariant fromConnections(boolean above, boolean below) {
        if (above && below) {
            return MIDDLE;
        } else if (above) {
            return LOWER;
        } else if (below) {
            return UPPER;
        } else {
            return NORMAL;
        }
    }
}
