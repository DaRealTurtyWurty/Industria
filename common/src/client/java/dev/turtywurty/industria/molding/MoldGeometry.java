package dev.turtywurty.industria.molding;

import com.mojang.blaze3d.platform.NativeImage;

import java.util.List;

public final class MoldGeometry {
    private MoldGeometry() {
    }

    public static boolean contains(List<MoldPoint> polygon, float x, float y) {
        boolean inside = false;
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            MoldPoint a = polygon.get(i);
            MoldPoint b = polygon.get(j);
            if ((a.y() > y) != (b.y() > y)) {
                float crossingX = a.x() + (y - a.y()) * (b.x() - a.x()) / (b.y() - a.y());
                if (x < crossingX) {
                    inside = !inside;
                }
            }
        }

        return inside;
    }

    public static NativeImage createMask(MoldPattern pattern, int completedPolygons, int width, int height) {
        var image = new NativeImage(width, height, true);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float px = (x + 0.5f) / width;
                float py = (y + 0.5f) / height;

                boolean inside = false;
                for (int i = 0; i < completedPolygons; i++) {
                    if (contains(pattern.polygons().get(i), px, py)) {
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
}
