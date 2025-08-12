package dev.turtywurty.industria.util;

public class ColourUtils {
    public static int lerpColour(int colour1, int colour2, float delta) {
        delta = Math.max(0.0f, Math.min(1.0f, delta));

        int a1 = (colour1 >> 24) & 0xff;
        int r1 = (colour1 >> 16) & 0xff;
        int g1 = (colour1 >> 8) & 0xff;
        int b1 = colour1 & 0xff;

        int a2 = (colour2 >> 24) & 0xff;
        int r2 = (colour2 >> 16) & 0xff;
        int g2 = (colour2 >> 8) & 0xff;
        int b2 = colour2 & 0xff;

        int a = (int) (a1 + (a2 - a1) * delta);
        int r = (int) (r1 + (r2 - r1) * delta);
        int g = (int) (g1 + (g2 - g1) * delta);
        int b = (int) (b1 + (b2 - b1) * delta);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
