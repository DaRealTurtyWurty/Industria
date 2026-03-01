package dev.turtywurty.industria.conveyor.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public record ConveyorOutput(
        String id,
        BlockPos deliveryPos,
        BlockPos expectedInputPos
) {
    public Direction inventoryInsertSide() {
        Vec3i delta = expectedInputPos.subtract(deliveryPos);
        return Direction.getApproximateNearest(delta.getX(), delta.getY(), delta.getZ());
    }
}
