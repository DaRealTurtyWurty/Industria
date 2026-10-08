package dev.turtywurty.industria.molding;

import java.util.Arrays;

public final class MoldCastShadows {
    private static final int GRID_SIZE = MoldCastSurface.RESOLUTION;
    private static final int SHADOW_SIZE = GRID_SIZE * 2;
    private static final int SHADOW_STRIDE = SHADOW_SIZE + 1;
    private static final float AMBIENT_LIGHT = 0.55f;
    private static final float LIGHT_ELEVATION = 1.0f;
    private static final float RAY_STEP = 0.5f / SHADOW_SIZE;
    private static final float DEPTH_BIAS = 0.0005f;
    private static final float[][] LIGHT_DIRECTIONS = {
            {-0.6000f, -0.8000f},
            {-0.6814f, -0.7319f},
            {-0.5123f, -0.8588f}
    };

    private MoldCastShadows() {
    }

    public static float[] calculate(MoldCastSurface.Snapshot surface) {
        float[] illumination = new float[(GRID_SIZE + 1) * (GRID_SIZE + 1)];
        Arrays.fill(illumination, 1.0f);

        boolean hasGrooves = false;
        for (int y = 0; y <= GRID_SIZE && !hasGrooves; y++) {
            for (int x = 0; x <= GRID_SIZE; x++) {
                if (surface.depth(x, y) > DEPTH_BIAS) {
                    hasGrooves = true;
                    break;
                }
            }
        }

        if (!hasGrooves)
            return illumination;

        float[] projectedDepths = project(surface);
        for (int y = 0; y <= GRID_SIZE; y++) {
            for (int x = 0; x <= GRID_SIZE; x++) {
                float depth = surface.depth(x, y);
                if (depth <= DEPTH_BIAS)
                    continue;

                int visibleRays = 0;
                for (float[] direction : LIGHT_DIRECTIONS) {
                    if (isVisible(projectedDepths, surface.positionX(x, y), surface.positionY(x, y),
                            depth, direction[0], direction[1])) {
                        visibleRays++;
                    }
                }

                float visibility = visibleRays / (float) LIGHT_DIRECTIONS.length;
                illumination[y * (GRID_SIZE + 1) + x] = AMBIENT_LIGHT + (1 - AMBIENT_LIGHT) * visibility;
            }
        }
        return illumination;
    }

    private static float[] project(MoldCastSurface.Snapshot surface) {
        float[] projectedDepths = new float[SHADOW_STRIDE * SHADOW_STRIDE];
        Arrays.fill(projectedDepths, Float.POSITIVE_INFINITY);
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                if (surface.removed(x, y))
                    continue;

                rasterize(projectedDepths, surface, x, y, x, y + 1, x + 1, y + 1);
                rasterize(projectedDepths, surface, x, y, x + 1, y + 1, x + 1, y);
            }
        }
        return projectedDepths;
    }

    private static void rasterize(float[] target, MoldCastSurface.Snapshot surface,
                                  int ax, int ay, int bx, int by, int cx, int cy) {
        float aX = surface.positionX(ax, ay);
        float aY = surface.positionY(ax, ay);
        float bX = surface.positionX(bx, by);
        float bY = surface.positionY(bx, by);
        float cX = surface.positionX(cx, cy);
        float cY = surface.positionY(cx, cy);
        float aDepth = surface.depth(ax, ay);
        float bDepth = surface.depth(bx, by);
        float cDepth = surface.depth(cx, cy);
        float denominator = (bY - cY) * (aX - cX) + (cX - bX) * (aY - cY);

        int minX = Math.max(0, (int) Math.floor(Math.min(aX, Math.min(bX, cX)) * SHADOW_SIZE));
        int maxX = Math.min(SHADOW_SIZE, (int) Math.ceil(Math.max(aX, Math.max(bX, cX)) * SHADOW_SIZE));
        int minY = Math.max(0, (int) Math.floor(Math.min(aY, Math.min(bY, cY)) * SHADOW_SIZE));
        int maxY = Math.min(SHADOW_SIZE, (int) Math.ceil(Math.max(aY, Math.max(bY, cY)) * SHADOW_SIZE));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float px = x / (float) SHADOW_SIZE;
                float py = y / (float) SHADOW_SIZE;
                float aWeight = ((bY - cY) * (px - cX) + (cX - bX) * (py - cY)) / denominator;
                float bWeight = ((cY - aY) * (px - cX) + (aX - cX) * (py - cY)) / denominator;
                float cWeight = 1 - aWeight - bWeight;
                if (aWeight < -0.0001f || bWeight < -0.0001f || cWeight < -0.0001f)
                    continue;

                float depth = aWeight * aDepth + bWeight * bDepth + cWeight * cDepth;
                int index = y * SHADOW_STRIDE + x;
                target[index] = Math.min(target[index], depth);
            }
        }
    }

    private static boolean isVisible(float[] depths, float x, float y, float depth,
                                     float directionX, float directionY) {
        for (float travel = RAY_STEP; travel * LIGHT_ELEVATION < depth; travel += RAY_STEP) {
            float sampleX = x + directionX * travel;
            float sampleY = y + directionY * travel;
            if (sampleX < 0 || sampleY < 0 || sampleX >= 1 || sampleY >= 1)
                break;

            float rayDepth = depth - travel * LIGHT_ELEVATION;
            if (sampleDepth(depths, sampleX, sampleY) + DEPTH_BIAS < rayDepth)
                return false;
        }
        return true;
    }

    private static float sampleDepth(float[] depths, float x, float y) {
        float gridX = x * SHADOW_SIZE;
        float gridY = y * SHADOW_SIZE;
        int left = (int) gridX;
        int top = (int) gridY;
        float a = depths[top * SHADOW_STRIDE + left];
        float b = depths[top * SHADOW_STRIDE + left + 1];
        float c = depths[(top + 1) * SHADOW_STRIDE + left];
        float d = depths[(top + 1) * SHADOW_STRIDE + left + 1];
        if (!Float.isFinite(a) || !Float.isFinite(b) || !Float.isFinite(c) || !Float.isFinite(d))
            return Float.POSITIVE_INFINITY;

        float fractionX = gridX - left;
        float fractionY = gridY - top;
        float upper = a + fractionX * (b - a);
        float lower = c + fractionX * (d - c);
        return upper + fractionY * (lower - upper);
    }
}
