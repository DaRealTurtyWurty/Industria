package dev.turtywurty.industria.molding;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record MoldTableVisualState(
    Identifier blankItem,
    Identifier moldId,
    int completedPolygons,
    boolean failed,
    List<Stroke> strokes
) {
    public static final int MAX_STROKES = 8192;
    private static final Codec<MoldPoint> POINT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.FLOAT.fieldOf("x").forGetter(MoldPoint::x),
        Codec.FLOAT.fieldOf("y").forGetter(MoldPoint::y)).apply(instance, MoldPoint::new));

    public static final Codec<MoldTableVisualState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Identifier.CODEC.fieldOf("blank").forGetter(MoldTableVisualState::blankItem),
        Identifier.CODEC.fieldOf("mold").forGetter(MoldTableVisualState::moldId),
        Codec.INT.fieldOf("completed").forGetter(MoldTableVisualState::completedPolygons),
        Codec.BOOL.fieldOf("failed").forGetter(MoldTableVisualState::failed),
        Stroke.CODEC.listOf().fieldOf("strokes").forGetter(MoldTableVisualState::strokes))
        .apply(instance, MoldTableVisualState::new));

    public MoldTableVisualState {
        strokes = List.copyOf(strokes);
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && blankItem.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public boolean isValid() {
        return MoldPatterns.find(moldId)
            .filter(pattern -> completedPolygons >= 0 && completedPolygons <= pattern.polygons().size())
            .isPresent() && strokes.size() <= MAX_STROKES && strokes.stream().allMatch(Stroke::isValid);
    }

    public record Stroke(MoldPoint from, MoldPoint to, float radius) {
        public static final Codec<Stroke> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            POINT_CODEC.fieldOf("from").forGetter(Stroke::from),
            POINT_CODEC.fieldOf("to").forGetter(Stroke::to),
            Codec.FLOAT.fieldOf("radius").forGetter(Stroke::radius)).apply(instance, Stroke::new));

        private boolean isValid() {
            return validPoint(from) && validPoint(to) && Float.isFinite(radius) && radius > 0 && radius <= 0.1f;
        }

        private static boolean validPoint(MoldPoint point) {
            return Float.isFinite(point.x()) && Float.isFinite(point.y())
                && point.x() >= -0.1f && point.x() <= 1.1f && point.y() >= -0.1f && point.y() <= 1.1f;
        }
    }
}
