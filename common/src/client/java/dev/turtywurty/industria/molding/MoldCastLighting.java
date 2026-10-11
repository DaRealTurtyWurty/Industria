package dev.turtywurty.industria.molding;

public final class MoldCastLighting {
    private static final float LIGHT_X = -0.6f;
    private static final float LIGHT_Y = -0.8f;
    private static final float WALL_SHADE = 0.45f;
    private static final float WALL_HIGHLIGHT = 0.35f;

    private MoldCastLighting() {
    }

    public static float wallFacing(float normalX, float normalY) {
        return Math.clamp(normalX * LIGHT_X + normalY * LIGHT_Y, -1, 1);
    }

    public static float brightness(float facing, float visibility) {
        return visibility * (1 + WALL_SHADE * Math.min(facing, 0));
    }

    public static float highlight(float facing, float visibility) {
        return WALL_HIGHLIGHT * Math.max(facing, 0) * visibility;
    }
}
