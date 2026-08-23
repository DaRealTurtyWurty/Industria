package dev.turtywurty.industria.multiblock;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;

/**
 * Represents a local direction relative to a multiblock structure.
 * The directions are defined in relation to the facing direction of the structure.
 */
public enum LocalDirection {
    UP, DOWN, FRONT, BACK, LEFT, RIGHT;

    public static final Codec<LocalDirection> CODEC = Codec.STRING.xmap(
            LocalDirection::valueOf,
            LocalDirection::name
    );

    /**
     * Converts this local direction to a world direction based on the given facing direction.
     *
     * @param facing The direction the multiblock structure is facing.
     * @return The corresponding world direction.
     */
    public Direction toWorld(Direction facing) {
        return switch (this) {
            case UP    -> Direction.UP;
            case DOWN  -> Direction.DOWN;
            case FRONT -> facing;
            case BACK  -> facing.getOpposite();
            case LEFT  -> facing.getCounterClockWise();
            case RIGHT -> facing.getClockWise();
        };
    }

    public Direction toWorld(LocalDirection facing) {
        return switch (this) {
            case UP    -> Direction.UP;
            case DOWN  -> Direction.DOWN;
            case FRONT -> facing.toWorld(Direction.NORTH);
            case BACK  -> facing.toWorld(Direction.SOUTH);
            case LEFT  -> facing.toWorld(Direction.WEST);
            case RIGHT -> facing.toWorld(Direction.EAST);
        };
    }
}
