package dev.turtywurty.industria.multiblock;

import net.minecraft.util.math.Direction;

public enum LocalDirection {
    UP, DOWN, FRONT, BACK, LEFT, RIGHT;

    public Direction toWorld(Direction facing) {
        return switch (this) {
            case UP    -> Direction.UP;
            case DOWN  -> Direction.DOWN;
            case FRONT -> facing;
            case BACK  -> facing.getOpposite();
            case LEFT  -> facing.rotateYCounterclockwise();
            case RIGHT -> facing.rotateYClockwise();
        };
    }
}
