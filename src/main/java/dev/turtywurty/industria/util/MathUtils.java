package dev.turtywurty.industria.util;

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
}
