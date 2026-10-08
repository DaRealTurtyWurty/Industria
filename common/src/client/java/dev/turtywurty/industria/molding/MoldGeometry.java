package dev.turtywurty.industria.molding;

import com.mojang.blaze3d.platform.NativeImage;
import dev.turtywurty.industria.Industria;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.InputStream;
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
        // Resolve each texture once for this operation, not once per output pixel.
        var cutouts = new ArrayList<BiPredicate<Float, Float>>();
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
}
