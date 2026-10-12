package dev.turtywurty.industria.molding;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

public record MoldPattern(
    Identifier id,
    List<List<MoldPoint>> polygons,
    Map<Integer, MoldTextureCutout> cutoutTextures,
    boolean split
) {
    public MoldPattern {
        polygons = polygons.stream().map(List::copyOf).toList();
        cutoutTextures = Map.copyOf(cutoutTextures);
    }

    public MoldPattern(Identifier id, List<List<MoldPoint>> polygons) {
        this(id, polygons, Map.of(), false);
    }

    public static boolean containsPolygon(List<MoldPoint> polygon, float x, float y) {
        boolean inside = false;
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            MoldPoint a = polygon.get(i);
            MoldPoint b = polygon.get(j);
            if ((a.y() > y) != (b.y() > y)
                && x < a.x() + (y - a.y()) * (b.x() - a.x()) / (b.y() - a.y())) {
                inside = !inside;
            }
        }
        return inside;
    }

    public Component getDisplayName() {
        return Component.translatable("mold." + this.id.getNamespace() + "." + this.id.getPath());
    }
}
