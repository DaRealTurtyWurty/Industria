package dev.turtywurty.industria.molding;

import com.mojang.blaze3d.platform.NativeImage;
import dev.turtywurty.industria.Industria;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public final class MoldGeometry {
    private MoldGeometry() {
    }

    public static boolean contains(List<MoldPoint> polygon, float x, float y) {
        return MoldCastSurface.contains(polygon, x, y);
    }

    public static NativeImage createMask(MoldPattern pattern, int completedPolygons, int width, int height) {
        List<BiPredicate<Float, Float>> cutouts = new ArrayList<>();
        for (int i = 0; i < completedPolygons; i++) {
            cutouts.add(createCutout(pattern, i));
        }

        var image = new NativeImage(width, height, true);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float px = (x + 0.5f) / width;
                float py = (y + 0.5f) / height;

                boolean inside = false;
                for (int i = 0; i < completedPolygons; i++) {
                    if (cutouts.get(i).test(px, py)) {
                        inside = true;
                        break;
                    }
                }

                if (inside) {
                    image.setPixel(x, y, 0x00000000);
                } else {
                    image.setPixel(x, y, 0xFFFFFFFF);
                }
            }
        }

        return image;
    }

    public static BiPredicate<Float, Float> createCutout(MoldPattern pattern, int polygonIndex) {
        MoldTextureCutout cutout = pattern.cutoutTextures().get(polygonIndex);
        if (cutout != null) {
            try (InputStream stream = Minecraft.getInstance().getResourceManager()
                    .getResourceOrThrow(cutout.texture()).open();
                 NativeImage image = NativeImage.read(stream)) {
                int width = image.getWidth();
                int height = image.getHeight();
                int[] pixels = new int[width * height];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        pixels[y * width + x] = image.getPixel(x, y);
                    }
                }

                return (x, y) -> cutout.contains(pixels, width, height, x, y);
            } catch (IOException exception) {
                Industria.LOGGER.error("Unable to load mold cutout texture {}", cutout.texture(), exception);
            }
        }

        List<MoldPoint> polygon = pattern.polygons().get(polygonIndex);
        return (x, y) -> MoldPattern.containsPolygon(polygon, x, y);
    }

    public static NativeImage createSilhouette(MoldPattern pattern, int width, int height) {
        NativeImage image = createMask(pattern, pattern.polygons().size(), width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setPixel(x, y, image.getPixel(x, y) == 0x00000000 ? 0xFFFFFFFF : 0x00000000);
            }
        }

        return image;
    }

    public static NativeImage createItemTexture(NativeImage material, MoldPattern pattern) {
        int width = material.getWidth();
        int height = material.getHeight();
        try (NativeImage mask = createMask(pattern, pattern.polygons().size(), width, height)) {
            var image = new NativeImage(width, height, true);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (mask.getPixel(x, y) == 0) {
                        image.setPixel(x, y, 0);
                    } else {
                        image.setPixel(x, y, material.getPixel(x, y));
                    }
                }
            }

            return image;
        }
    }

    public static NativeImage createItemTexture(NativeImage material, MoldPattern pattern, MoldPiece piece) {
        NativeImage image = createItemTexture(material, pattern);
        if (piece == MoldPiece.WHOLE)
            return image;

        boolean[] firstPiece = findFirstPiece(image);
        for (int index = 0; index < firstPiece.length; index++) {
            int x = index % image.getWidth();
            int y = index / image.getWidth();
            boolean belongsToFirst = firstPiece[y * image.getWidth() + x];
            if ((piece == MoldPiece.HALF_1 && !belongsToFirst) || (piece == MoldPiece.HALF_2 && belongsToFirst)) {
                image.setPixel(x, y, 0);
            }
        }

        return image;
    }

    private static boolean isSolid(NativeImage image, int x, int y) {
        if (x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight())
            return false;

        return image.getPixel(x, y) >>> 24 != 0;
    }

    private static void visitPiecePixel(NativeImage image, int x, int y, boolean[] piece, ArrayDeque<Integer> pending) {
        if (!isSolid(image, x, y))
            return;

        int index = y * image.getWidth() + x;
        if (piece[index])
            return;

        piece[index] = true;
        pending.addLast(index);
    }

    private static boolean[] findFirstPiece(NativeImage image) {
        boolean[] piece = new boolean[image.getWidth() * image.getHeight()];
        ArrayDeque<Integer> pending = new ArrayDeque<>();

        for (int index = 0; index < piece.length; index++) {
            int x = index % image.getWidth();
            int y = index / image.getWidth();
            if (isSolid(image, x, y)) {
                visitPiecePixel(image, x, y, piece, pending);
                break;
            }
        }

        while (!pending.isEmpty()) {
            int index = pending.removeFirst();
            int x = index % image.getWidth();
            int y = index / image.getWidth();

            visitPiecePixel(image, x - 1, y, piece, pending);
            visitPiecePixel(image, x + 1, y, piece, pending);
            visitPiecePixel(image, x, y - 1, piece, pending);
            visitPiecePixel(image, x, y + 1, piece, pending);
        }

        return piece;
    }
}
