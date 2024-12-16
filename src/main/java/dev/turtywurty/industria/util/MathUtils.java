package dev.turtywurty.industria.util;

import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class MathUtils {
    public static Direction getRelativeDirection(@Nullable Direction direction, @Nullable Direction facing) {
        if(direction == null)
            return null;
        else if(facing == null)
            return direction;
        else if(direction.getAxis().isVertical())
            return direction;

        Direction relative = direction;
        for (int i = 0; i < facing.getHorizontal(); i++) {
            relative = relative.rotateYClockwise();
        }

        return relative;
    }
}
