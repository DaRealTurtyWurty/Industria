package dev.turtywurty.industria.molding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MoldWorldMesh {
    public static final float THICKNESS = 1f / 14f;
    private static final int SIZE = MoldCastSurface.RESOLUTION;
    private final MoldCastSurface.Snapshot surface;
    private final int textureWidth;
    private final int textureHeight;
    private final boolean[] opaque;
    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Vertex> view = Collections.unmodifiableList(vertices);

    public MoldWorldMesh(MoldCastSurface.Snapshot surface, int textureWidth, int textureHeight, boolean[] opaque) {
        this.surface = surface;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.opaque = opaque.clone();
        for (int y = 0; y < textureHeight; y++) {
            for (int x = 0; x < textureWidth; x++) {
                addRegion(x * SIZE / textureWidth, y * SIZE / textureHeight,
                    (x + 1) * SIZE / textureWidth, (y + 1) * SIZE / textureHeight);
            }
        }
        addWalls();
    }

    public List<Vertex> vertices() {
        return view;
    }

    private boolean removed(int x, int y) {
        return x < 0 || y < 0 || x >= SIZE || y >= SIZE || surface.removed(x, y)
            || !opaque[(y * textureHeight / SIZE) * textureWidth + x * textureWidth / SIZE];
    }

    private void addRegion(int x0, int y0, int x1, int y1) {
        if (x0 == x1 || y0 == y1)
            return;

        boolean empty = true;
        boolean solid = true;
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                boolean removed = removed(x, y);
                empty &= removed;
                solid &= !removed;
            }
        }
        if (empty)
            return;

        boolean flat = solid;
        for (int y = y0; flat && y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                if (surface.depth(x, y) != 0 || surface.positionX(x, y) != x / (float) SIZE
                    || surface.positionY(x, y) != y / (float) SIZE) {
                    flat = false;
                    break;
                }
            }
        }
        if (flat || (x1 - x0 == 1 && y1 - y0 == 1)) {
            if (flat) {
                addQuad(top(x0, y0), top(x1, y0), top(x1, y1), top(x0, y1));
            } else {
                addTriangle(top(x0, y0), top(x1, y0), top(x1, y1));
                addTriangle(top(x0, y0), top(x1, y1), top(x0, y1));
            }
            addQuad(bottom(x0, y0), bottom(x0, y1), bottom(x1, y1), bottom(x1, y0));
            return;
        }

        int midX = x0 + Math.max(1, (x1 - x0) / 2);
        int midY = y0 + Math.max(1, (y1 - y0) / 2);
        addRegion(x0, y0, midX, midY);
        addRegion(midX, y0, x1, midY);
        addRegion(x0, midY, midX, y1);
        addRegion(midX, midY, x1, y1);
    }

    private void addWalls() {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                if (removed(x, y))
                    continue;

                if (removed(x - 1, y)) {
                    wall(x, y, x, y + 1, x, y);
                }

                if (removed(x + 1, y)) {
                    wall(x + 1, y + 1, x + 1, y, x, y);
                }

                if (removed(x, y - 1)) {
                    wall(x + 1, y, x, y, x, y);
                }

                if (removed(x, y + 1)) {
                    wall(x, y + 1, x + 1, y + 1, x, y);
                }
            }
        }
    }

    private void wall(int ax, int ay, int bx, int by, int cellX, int cellY) {
        float u = (cellX + 0.5f) / SIZE;
        float v = (cellY + 0.5f) / SIZE;

        addQuad(point(ax, ay, -surface.depth(ax, ay), u, v),
            point(bx, by, -surface.depth(bx, by), u, v),
            point(bx, by, -THICKNESS, u, v), point(ax, ay, -THICKNESS, u, v));
    }

    private Point top(int x, int y) {
        return point(x, y, -surface.depth(x, y), x / (float) SIZE, y / (float) SIZE);
    }

    private Point bottom(int x, int y) {
        return point(x, y, -THICKNESS, x / (float) SIZE, y / (float) SIZE);
    }

    private Point point(int x, int y, float z, float u, float v) {
        return new Point(surface.positionX(x, y) - 0.5f, surface.positionY(x, y) - 0.5f, z,
            u, v, surface.illumination(x, y));
    }

    private void addTriangle(Point a, Point b, Point c) {
        addQuad(a, b, c, c);
    }

    private void addQuad(Point a, Point b, Point c, Point d) {
        float abx = b.x - a.x;
        float aby = b.y - a.y;
        float abz = b.z - a.z;
        float acx = c.x - a.x;
        float acy = c.y - a.y;
        float acz = c.z - a.z;
        float nx = aby * acz - abz * acy;
        float ny = abz * acx - abx * acz;
        float nz = abx * acy - aby * acx;
        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length == 0)
            return;

        nx /= length;
        ny /= length;
        nz /= length;
        vertices.add(a.vertex(nx, ny, nz));
        vertices.add(b.vertex(nx, ny, nz));
        vertices.add(c.vertex(nx, ny, nz));
        vertices.add(d.vertex(nx, ny, nz));
    }

    private record Point(float x, float y, float z, float u, float v, float shade) {
        private Vertex vertex(float nx, float ny, float nz) {
            return new Vertex(x, y, z, u, v, nx, ny, nz, shade);
        }
    }

    public record Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz, float shade) {
    }
}
