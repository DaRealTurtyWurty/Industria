package dev.turtywurty.industria.connected;

public record ConnectedTextureTile(int column, int row) {
    public ConnectedTextureTile {
        if (column < 0 || column > 11)
            throw new IllegalArgumentException("Column must be between 0 and 11");

        if (row < 0 || row > 3)
            throw new IllegalArgumentException("Row must be between 0 and 3");
    }

    public float minU() {
        return column / 12f;
    }

    public float maxU() {
        return (column + 1) / 12f;
    }

    public float minV() {
        return row / 4f;
    }

    public float maxV() {
        return (row + 1) / 4f;
    }

    public int index() {
        return row * 12 + column;
    }
}
