package dev.turtywurty.industria.molding;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiPredicate;

public final class MoldCastSurface {
    public static final int RESOLUTION = 192;

    private static final float GROOVE_DEPTH = 0.032f;
    private static final float FLOOR_RADIUS = 0.50f;
    private static final float MATERIAL_DISPLACEMENT = 0.30f;

    private final float[] depths = new float[(RESOLUTION + 1) * (RESOLUTION + 1)];
    private final float[] offsetsX = new float[depths.length];
    private final float[] offsetsY = new float[depths.length];
    private final float[] influences = new float[depths.length];
    private final boolean[] removed = new boolean[RESOLUTION * RESOLUTION];

    private Snapshot snapshot;

    public void clear() {
        clearGrooves();
        Arrays.fill(removed, false);
    }

    private void clearGrooves() {
        Arrays.fill(depths, 0);
        Arrays.fill(offsetsX, 0);
        Arrays.fill(offsetsY, 0);
        Arrays.fill(influences, 0);
        snapshot = null;
    }

    public void carve(MoldPoint from, MoldPoint to, float radius) {
        if (radius <= 0)
            return;

        int minX = Math.max(0, (int) Math.floor((Math.min(from.x(), to.x()) - radius) * RESOLUTION));
        int maxX = Math.min(RESOLUTION, (int) Math.ceil((Math.max(from.x(), to.x()) + radius) * RESOLUTION));
        int minY = Math.max(0, (int) Math.floor((Math.min(from.y(), to.y()) - radius) * RESOLUTION));
        int maxY = Math.min(RESOLUTION, (int) Math.ceil((Math.max(from.y(), to.y()) + radius) * RESOLUTION));
        float dx = to.x() - from.x();
        float dy = to.y() - from.y();
        float lengthSquared = dx * dx + dy * dy;
        boolean changed = false;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float px = x / (float) RESOLUTION;
                float py = y / (float) RESOLUTION;
                float t = lengthSquared == 0 ? 0 : Math.clamp(
                        ((px - from.x()) * dx + (py - from.y()) * dy) / lengthSquared, 0, 1);
                float towardX = from.x() + t * dx - px;
                float towardY = from.y() + t * dy - py;
                float distance = (float) Math.hypot(towardX, towardY);
                int index = y * (RESOLUTION + 1) + x;

                if (distance < radius) {
                    float wallPosition = (distance / radius - FLOOR_RADIUS) / (1 - FLOOR_RADIUS);
                    float depth = GROOVE_DEPTH * (1 - Math.clamp(wallPosition, 0, 1));
                    if (depth > depths[index]) {
                        depths[index] = depth;
                        changed = true;
                    }
                }

                if (distance >= radius || x == 0 || y == 0 || x == RESOLUTION || y == RESOLUTION)
                    continue;

                float influence = 1 - distance / radius;
                if (influence > influences[index]) {
                    float floorRadius = radius * FLOOR_RADIUS;
                    float profile = distance <= floorRadius
                            ? distance / floorRadius
                            : (radius - distance) / (radius - floorRadius);
                    float displacement = radius * MATERIAL_DISPLACEMENT * profile;
                    float directionScale = distance == 0 ? 0 : displacement / distance;
                    offsetsX[index] = -towardX * directionScale;
                    offsetsY[index] = -towardY * directionScale;
                    influences[index] = influence;
                    changed = true;
                }
            }
        }

        if (changed) {
            snapshot = null;
        }
    }

    public void removePolygon(List<MoldPoint> polygon) {
        removeCutout((x, y) -> contains(polygon, x, y));
    }

    public void removePolygon(MoldPattern pattern, int polygonIndex) {
        removeCutout(MoldGeometry.createCutout(pattern, polygonIndex));
    }

    private void removeCutout(BiPredicate<Float, Float> contains) {
        for (int y = 0; y < RESOLUTION; y++) {
            for (int x = 0; x < RESOLUTION; x++) {
                if (contains.test((x + 0.5f) / RESOLUTION, (y + 0.5f) / RESOLUTION)) {
                    removed[y * RESOLUTION + x] = true;
                }
            }
        }

        clearGrooves();
    }

    static boolean contains(List<MoldPoint> polygon, float x, float y) {
        return MoldPattern.containsPolygon(polygon, x, y);
    }

    public Snapshot snapshot() {
        if (snapshot == null) {
            snapshot = new Snapshot(depths.clone(), offsetsX.clone(), offsetsY.clone(), removed.clone());
        }

        return snapshot;
    }

    public static final class Snapshot {
        private final float[] depths;
        private final float[] offsetsX;
        private final float[] offsetsY;
        private final boolean[] removed;
        private final float displacementScale;
        private final float[] illumination;

        private Snapshot(float[] depths, float[] offsetsX, float[] offsetsY, boolean[] removed) {
            this.depths = depths;
            this.offsetsX = offsetsX;
            this.offsetsY = offsetsY;
            this.removed = removed;

            float scale = 1.0f;
            while (!hasValidCells(scale)) {
                scale *= 0.5f;
            }
            this.displacementScale = scale;
            this.illumination = MoldCastShadows.calculate(this);
        }

        public float depth(int x, int y) {
            return depths[y * (RESOLUTION + 1) + x];
        }

        public boolean removed(int x, int y) {
            return removed[y * RESOLUTION + x];
        }

        public float illumination(int x, int y) {
            return illumination[y * (RESOLUTION + 1) + x];
        }

        public float positionX(int x, int y) {
            return positionX(x, y, displacementScale);
        }

        public float positionY(int x, int y) {
            return positionY(x, y, displacementScale);
        }

        private float positionX(int x, int y, float scale) {
            return x / (float) RESOLUTION + offsetsX[y * (RESOLUTION + 1) + x] * scale;
        }

        private float positionY(int x, int y, float scale) {
            return y / (float) RESOLUTION + offsetsY[y * (RESOLUTION + 1) + x] * scale;
        }

        private boolean hasValidCells(float scale) {
            float minimumArea = 0.05f / (RESOLUTION * RESOLUTION);
            for (int y = 0; y < RESOLUTION; y++) {
                for (int x = 0; x < RESOLUTION; x++) {
                    float ax = positionX(x, y, scale);
                    float ay = positionY(x, y, scale);
                    float bx = positionX(x, y + 1, scale);
                    float by = positionY(x, y + 1, scale);
                    float cx = positionX(x + 1, y + 1, scale);
                    float cy = positionY(x + 1, y + 1, scale);
                    float dx = positionX(x + 1, y, scale);
                    float dy = positionY(x + 1, y, scale);

                    float firstArea = (cx - ax) * (by - ay) - (cy - ay) * (bx - ax);
                    float secondArea = (dx - ax) * (cy - ay) - (dy - ay) * (cx - ax);
                    if (firstArea < minimumArea || secondArea < minimumArea)
                        return false;
                }
            }

            return true;
        }
    }
}
