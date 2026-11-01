package dev.turtywurty.industria.connected;

public enum ConnectedTextureLayoutType {
    CTM47(12, 4, 0),
    HORIZONTAL_STRIP(4, 1, 48),
    VERTICAL_STRIP(1, 4, 52);

    private final int columns;
    private final int rows;
    private final int indexOffset;

    ConnectedTextureLayoutType(int columns, int rows, int indexOffset) {
        this.columns = columns;
        this.rows = rows;
        this.indexOffset = indexOffset;
    }

    public int tileCount() {
        return this.columns * this.rows;
    }

    public int getColumns() {
        return this.columns;
    }

    public int getRows() {
        return this.rows;
    }

    public int getIndexOffset() {
        return this.indexOffset;
    }
}
