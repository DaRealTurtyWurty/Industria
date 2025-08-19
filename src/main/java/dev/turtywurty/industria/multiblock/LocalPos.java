package dev.turtywurty.industria.multiblock;

import net.minecraft.util.math.Vec3i;

public record LocalPos(int x, int y, int z) {
    public LocalPos(Vec3i vec) {
        this(vec.getX(), vec.getY(), vec.getZ());
    }

    public static LocalPos from(Vec3i vec) {
        return new LocalPos(vec.getX(), vec.getY(), vec.getZ());
    }

    public static LocalPos from(int x, int y, int z) {
        return new LocalPos(x, y, z);
    }

    public Vec3i toVec3i() {
        return new Vec3i(x, y, z);
    }

    public boolean isCenterColumn() {
        return Multiblockable.isCenterColumn(this);
    }
}
