package dev.turtywurty.industria.molding;

import net.minecraft.resources.Identifier;

/** Positions a texture's opaque pixels on the cast. */
public record MoldTextureCutout(Identifier texture, float left, float top, float width, float height) {
    public MoldPoint point(float x, float y) {
        return new MoldPoint(left + x * width, top + y * height);
    }

    public boolean contains(int[] pixels, int pixelWidth, int pixelHeight, float x, float y) {
        if (x < left || y < top || x >= left + width || y >= top + height)
            return false;
        int column = Math.min(pixelWidth - 1, (int) ((x - left) / width * pixelWidth));
        int row = Math.min(pixelHeight - 1, (int) ((y - top) / height * pixelHeight));
        return (pixels[row * pixelWidth + column] >>> 24) != 0;
    }
}
