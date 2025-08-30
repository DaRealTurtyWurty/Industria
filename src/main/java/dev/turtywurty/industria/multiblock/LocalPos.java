package dev.turtywurty.industria.multiblock;

import net.minecraft.util.math.Vec3i;

/**
 * Represents a position relative to a multiblock structure.
 * This position is local to the multiblock and does not include the world offset.
 */
public record LocalPos(int x, int y, int z) {
    /**
     * Creates a new LocalPos instance.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    public LocalPos {
    }

    /**
     * Creates a new LocalPos instance from a Vec3i.
     *
     * @param vec the Vec3i to convert
     */
    public LocalPos(Vec3i vec) {
        this(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Creates a LocalPos from a Vec3i.
     *
     * @param vec the Vec3i to convert
     * @return a new LocalPos instance
     */
    public static LocalPos from(Vec3i vec) {
        return new LocalPos(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Creates a LocalPos from individual coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @return a new LocalPos instance
     */
    public static LocalPos from(int x, int y, int z) {
        return new LocalPos(x, y, z);
    }

    /**
     * Converts this LocalPos to a Vec3i.
     *
     * @return a Vec3i representing this LocalPos
     */
    public Vec3i toVec3i() {
        return new Vec3i(x, y, z);
    }

    /**
     * Checks if this LocalPos is the center of the multiblock structure.
     *
     * @return true if this position is the center, false otherwise
     */
    public boolean isCenterColumn() {
        return x == 0 && z == 0;
    }
}
