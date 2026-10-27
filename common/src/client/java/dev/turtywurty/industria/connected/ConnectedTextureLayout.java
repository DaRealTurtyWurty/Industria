package dev.turtywurty.industria.connected;

import java.util.Map;
import java.util.Optional;

import static dev.turtywurty.industria.connected.ConnectedTextureMask.*;

public class ConnectedTextureLayout {
    private static final Map<Integer, ConnectedTextureTile> TILE_MASK_MAP = Map.ofEntries(
            // No connections or horizontal connections only.
            Map.entry(CENTER, new ConnectedTextureTile(0, 3)),
            Map.entry(CENTER | RIGHT, new ConnectedTextureTile(1, 3)),
            Map.entry(CENTER | LEFT | RIGHT, new ConnectedTextureTile(2, 3)),
            Map.entry(CENTER | LEFT, new ConnectedTextureTile(3, 3)),

            // Vertical connections only.
            Map.entry(CENTER | BOTTOM, new ConnectedTextureTile(0, 0)),
            Map.entry(CENTER | TOP | BOTTOM, new ConnectedTextureTile(0, 1)),
            Map.entry(CENTER | TOP, new ConnectedTextureTile(0, 2)),

            // Two adjacent straight connections with their diagonal connection.
            Map.entry(CENTER | BOTTOM | RIGHT | BOTTOM_RIGHT, new ConnectedTextureTile(8, 0)),
            Map.entry(CENTER | BOTTOM | LEFT | BOTTOM_LEFT, new ConnectedTextureTile(11, 0)),
            Map.entry(CENTER | TOP | RIGHT | TOP_RIGHT, new ConnectedTextureTile(8, 3)),
            Map.entry(CENTER | TOP | LEFT | TOP_LEFT, new ConnectedTextureTile(11, 3)),

            // Panel edges and centre with every applicable diagonal connected.
            Map.entry(CENTER | LEFT | RIGHT | BOTTOM | BOTTOM_LEFT | BOTTOM_RIGHT, new ConnectedTextureTile(10, 0)),
            Map.entry(CENTER | TOP | RIGHT | BOTTOM | TOP_RIGHT | BOTTOM_RIGHT, new ConnectedTextureTile(8, 1)),
            Map.entry(CENTER | TOP | RIGHT | LEFT | BOTTOM | TOP_LEFT | TOP_RIGHT | BOTTOM_LEFT | BOTTOM_RIGHT, new ConnectedTextureTile(9, 2)),
            Map.entry(CENTER | TOP | LEFT | BOTTOM | TOP_LEFT | BOTTOM_LEFT, new ConnectedTextureTile(11, 2)),
            Map.entry(CENTER | TOP | LEFT | RIGHT | TOP_LEFT | TOP_RIGHT, new ConnectedTextureTile(9, 3)),

            // Two or more straight connections without diagonal connections.
            Map.entry(CENTER | RIGHT | BOTTOM, new ConnectedTextureTile(1, 0)),
            Map.entry(CENTER | LEFT | RIGHT | BOTTOM, new ConnectedTextureTile(2, 0)),
            Map.entry(CENTER | LEFT | BOTTOM, new ConnectedTextureTile(3, 0)),
            Map.entry(CENTER | TOP | RIGHT | BOTTOM, new ConnectedTextureTile(1, 1)),
            Map.entry(CENTER | TOP | LEFT | RIGHT | BOTTOM, new ConnectedTextureTile(2, 1)),
            Map.entry(CENTER | TOP | LEFT | BOTTOM, new ConnectedTextureTile(3, 1)),
            Map.entry(CENTER | TOP | RIGHT, new ConnectedTextureTile(1, 2)),
            Map.entry(CENTER | TOP | LEFT | RIGHT, new ConnectedTextureTile(2, 2)),
            Map.entry(CENTER | TOP | LEFT, new ConnectedTextureTile(3, 2)),

            // Three straight connections with one diagonal connection.
            Map.entry(CENTER | LEFT | RIGHT | BOTTOM | BOTTOM_RIGHT, new ConnectedTextureTile(5, 0)),
            Map.entry(CENTER | LEFT | RIGHT | BOTTOM | BOTTOM_LEFT, new ConnectedTextureTile(6, 0)),
            Map.entry(CENTER | TOP | RIGHT | BOTTOM | BOTTOM_RIGHT, new ConnectedTextureTile(4, 1)),
            Map.entry(CENTER | TOP | LEFT | BOTTOM | BOTTOM_LEFT, new ConnectedTextureTile(7, 1)),
            Map.entry(CENTER | TOP | RIGHT | BOTTOM | TOP_RIGHT, new ConnectedTextureTile(4, 2)),
            Map.entry(CENTER | TOP | LEFT | BOTTOM | TOP_LEFT, new ConnectedTextureTile(7, 2)),
            Map.entry(CENTER | TOP | LEFT | RIGHT | TOP_RIGHT, new ConnectedTextureTile(5, 3)),
            Map.entry(CENTER | TOP | LEFT | RIGHT | TOP_LEFT, new ConnectedTextureTile(6, 3)),

            // Four straight connections with one diagonal connection.
            Map.entry(CENTER | TOP | LEFT | RIGHT | BOTTOM | TOP_LEFT, new ConnectedTextureTile(4, 0)),
            Map.entry(CENTER | TOP | LEFT | RIGHT | BOTTOM | TOP_RIGHT, new ConnectedTextureTile(7, 0)),
            Map.entry(CENTER | TOP | LEFT | RIGHT | BOTTOM | BOTTOM_LEFT, new ConnectedTextureTile(4, 3)),
            Map.entry(CENTER | TOP | LEFT | RIGHT | BOTTOM | BOTTOM_RIGHT, new ConnectedTextureTile(7, 3)),

            // Four straight connections with two diagonal connections.
            Map.entry(CENTER | TOP | LEFT | RIGHT | BOTTOM | BOTTOM_LEFT | BOTTOM_RIGHT, new ConnectedTextureTile(9, 0)),
            Map.entry(CENTER | TOP | LEFT | RIGHT | BOTTOM | TOP_LEFT | TOP_RIGHT, new ConnectedTextureTile(10, 3)),
            Map.entry(CENTER | TOP | LEFT | RIGHT | BOTTOM | TOP_RIGHT | BOTTOM_LEFT, new ConnectedTextureTile(9, 1)),
            Map.entry(CENTER | TOP | LEFT | RIGHT | BOTTOM | TOP_LEFT | BOTTOM_LEFT, new ConnectedTextureTile(11, 1)),
            Map.entry(CENTER | TOP | LEFT | RIGHT | BOTTOM | TOP_RIGHT | BOTTOM_RIGHT, new ConnectedTextureTile(8, 2)),
            Map.entry(CENTER | TOP | LEFT | RIGHT | BOTTOM | TOP_LEFT | BOTTOM_RIGHT, new ConnectedTextureTile(10, 2)),

            // Four straight connections with three diagonal connections.
            Map.entry(CENTER | TOP | LEFT | RIGHT | BOTTOM | TOP_RIGHT | BOTTOM_LEFT | BOTTOM_RIGHT, new ConnectedTextureTile(5, 1)),
            Map.entry(CENTER | TOP | LEFT | RIGHT | BOTTOM | TOP_LEFT | BOTTOM_LEFT | BOTTOM_RIGHT, new ConnectedTextureTile(6, 1)),
            Map.entry(CENTER | TOP | LEFT | RIGHT | BOTTOM | TOP_LEFT | TOP_RIGHT | BOTTOM_RIGHT, new ConnectedTextureTile(5, 2)),
            Map.entry(CENTER | TOP | LEFT | RIGHT | BOTTOM | TOP_LEFT | TOP_RIGHT | BOTTOM_LEFT, new ConnectedTextureTile(6, 2))
    );

    private static final ConnectedTextureTile[] TILES_BY_INDEX = createTileIndex();

    public static ConnectedTextureTile getTile(int mask) {
        return Optional.ofNullable(TILE_MASK_MAP.get(mask))
                .orElseThrow(() -> new IllegalArgumentException("Invalid mask: " + mask));
    }

    public static ConnectedTextureTile getTileByIndex(int index) {
        if (index < 0 || index >= TILES_BY_INDEX.length)
            throw new IllegalArgumentException("Invalid index: " + index);

        ConnectedTextureTile tile = TILES_BY_INDEX[index];
        if (tile == null)
            throw new IllegalArgumentException("Unused tile index: " + index);

        return tile;
    }

    private static ConnectedTextureTile[] createTileIndex() {
        ConnectedTextureTile[] tiles = new ConnectedTextureTile[48];
        for (ConnectedTextureTile tile : TILE_MASK_MAP.values()) {
            int index = tile.index();
            tiles[index] = tile;
        }

        return tiles;
    }
}
