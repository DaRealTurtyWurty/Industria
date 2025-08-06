package dev.turtywurty.industria.util;

import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class MathUtils {
    public static @Nullable Direction getRelativeDirection(@Nullable Direction direction, @Nullable Direction facing) {
        if(direction == null || facing == null || direction.getAxis().isVertical())
            return direction;

        Direction relative = direction;
        int rotations = (facing.getHorizontalQuarterTurns() - Direction.NORTH.getHorizontalQuarterTurns() + 4) % 4;
        for (int i = 0; i < rotations; i++) {
            relative = relative.rotateYClockwise();
        }

        return relative;
    }

    public static Box rotateBox(Box box, Direction direction) {
        if (direction == null || direction.getAxis().isVertical())
            return box;

        double cx = (box.minX + box.maxX) * 0.5;
        double cy = (box.minY + box.maxY) * 0.5;
        double cz = (box.minZ + box.maxZ) * 0.5;

        // Precompute trig
        double rad = Math.toRadians(direction.getPositiveHorizontalDegrees());
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        // Get original corner offsets from center
        double x1 = box.minX - cx;
        double y1 = box.minY - cy;
        double z1 = box.minZ - cz;
        double x2 = box.maxX - cx;
        double y2 = box.maxY - cy;
        double z2 = box.maxZ - cz;

        // Rotate each corner around Y axis
        double rx1 = x1 * cos - z1 * sin;
        double rz1 = x1 * sin + z1 * cos;
        double rx2 = x2 * cos - z2 * sin;
        double rz2 = x2 * sin + z2 * cos;

        // Build and return new Box by translating back to world coords
        return new Box(
                cx + rx1, cy + y1, cz + rz1,
                cx + rx2, cy + y2, cz + rz2
        );
    }
}
