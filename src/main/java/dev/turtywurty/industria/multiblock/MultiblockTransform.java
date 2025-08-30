package dev.turtywurty.industria.multiblock;

import net.minecraft.util.math.AxisRotation;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public record MultiblockTransform(AxisRotation rotation, MirrorMode mirrorMode) {
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

    public Direction applyToFace(Direction face) {
        Direction direction = face;

        if(mirrorMode == MirrorMode.X) {
            if(direction == Direction.EAST || direction == Direction.WEST)
                direction = direction.getOpposite();
        } else if (mirrorMode == MirrorMode.Z) {
            if(direction == Direction.NORTH || direction == Direction.SOUTH)
                direction = direction.getOpposite();
        }

        for (int i = 0; i < rotation.index; i++) {
            direction = direction.rotateYClockwise();
        }

        return direction;
    }
}
