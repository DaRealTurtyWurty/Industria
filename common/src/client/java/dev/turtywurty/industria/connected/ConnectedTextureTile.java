package dev.turtywurty.industria.connected;

import java.util.Objects;

public record ConnectedTextureTile(int column, int row, ConnectedTextureLayoutType layout) {
    public ConnectedTextureTile {
        Objects.requireNonNull(layout, "layout cannot be null");

        if (column < 0 || column >= layout.getColumns())
            throw new IllegalArgumentException("Column must be between 0 and " + (layout.getColumns() - 1));

        if (row < 0 || row >= layout.getRows())
            throw new IllegalArgumentException("Row must be between 0 and " + (layout.getRows() - 1));
    }

    public ConnectedTextureTile(int column, int row) {
        this(column, row, ConnectedTextureLayoutType.CTM47);
    }

    public float minU() {
        return column / (float) layout.getColumns();
    }

    public float maxU() {
        return (column + 1) / (float) layout.getColumns();
    }

    public float minV() {
        return row / (float) layout.getRows();
    }

    public float maxV() {
        return (row + 1) / (float) layout.getRows();
    }

    public int index() {
        return layout.getIndexOffset() + (row * layout.getColumns()) + column;
    }
}
