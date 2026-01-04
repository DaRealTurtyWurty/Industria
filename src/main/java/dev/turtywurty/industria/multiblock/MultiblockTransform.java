package dev.turtywurty.industria.multiblock;

import com.mojang.math.Quadrant;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public record MultiblockTransform(Quadrant rotation, MirrorMode mirrorMode) {
    public Vec3i applyToSize(Vec3i size) {
        return switch (rotation) {
            case R90, R270 -> new Vec3i(size.getZ(), size.getY(), size.getX());
            default -> size;
        };
    }

    public Vec3i applyToLocal(Vec3i size, int anchorX, int anchorY, int anchorZ) {
        return applyToLocal(size.getX(), size.getZ(), anchorX, anchorY, anchorZ);
    }

    public Vec3i applyToLocal(Vec3i size, Vec3i anchor) {
        return applyToLocal(size.getX(), size.getZ(), anchor.getX(), anchor.getY(), anchor.getZ());
    }

    public Vec3i applyToLocal(int sizeX, int sizeZ, int anchorX, int anchorY, int anchorZ) {
        int localX = anchorX, localZ = anchorZ;
        if (mirrorMode == MirrorMode.X) {
            localX = sizeX - 1 - anchorX;
        } else if (mirrorMode == MirrorMode.Z) {
            localZ = sizeZ - 1 - anchorZ;
        }

        int rotatedX = localX, rotatedZ = localZ;
        switch (rotation) {
            case R0 -> {}
            case R90 -> {
                rotatedX = sizeZ - 1 - localZ;
                rotatedZ = localX;
            }
            case R180 -> {
                rotatedX = sizeX - 1 - localX;
                rotatedZ = sizeZ - 1 - localZ;
            }
            case R270 -> {
                rotatedX = localZ;
                rotatedZ = sizeX - 1 - localX;
            }
        }

        return new Vec3i(rotatedX, anchorY, rotatedZ);
    }

    public Vec3i applyInverseToLocal(Vec3i size, Vec3i pos) {
        return applyInverseToLocal(size, pos.getX(), pos.getY(), pos.getZ());
    }

    public Vec3i applyInverseToLocal(Vec3i size, int x, int y, int z) {
        int localX = x;
        int localZ = z;

        switch (rotation) {
            case R0 -> {}
            case R90 -> {
                localX = z;
                localZ = size.getZ() - 1 - x;
            }
            case R180 -> {
                localX = size.getX() - 1 - x;
                localZ = size.getZ() - 1 - z;
            }
            case R270 -> {
                localX = size.getX() - 1 - z;
                localZ = x;
            }
        }

        if (mirrorMode == MirrorMode.X) {
            localX = size.getX() - 1 - localX;
        } else if (mirrorMode == MirrorMode.Z) {
            localZ = size.getZ() - 1 - localZ;
        }

        return new Vec3i(localX, y, localZ);
    }

    public Direction applyToFace(Direction face) {
        Direction direction = face;

        if(mirrorMode == MirrorMode.X) {
            if(direction == Direction.EAST || direction == Direction.WEST)
                direction = direction.getOpposite();
        } else if (mirrorMode == MirrorMode.Z) {
            if(direction == Direction.NORTH || direction == Direction.SOUTH)
                direction = direction.getOpposite();
        }

        for (int i = 0; i < rotation.shift; i++) {
            direction = direction.getClockWise();
        }

        return direction;
    }
}
