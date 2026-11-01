package dev.turtywurty.industria.connected;

public enum ConnectedTextureAxis {
    BOTH,
    HORIZONTAL,
    VERTICAL;

    public boolean allowsHorizontal() {
        return this == BOTH || this == HORIZONTAL;
    }

    public boolean allowsVertical() {
        return this == BOTH || this == VERTICAL;
    }
}
