package dev.turtywurty.industria.connected;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class ConnectedTextureNeighborhood {
    private final BlockGetter level;
    private final Block targetBlock;
    private final int x, y, z;

    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

    private int checkedMask;
    private int connectedMask;

    public ConnectedTextureNeighborhood(
            BlockGetter level,
            BlockPos pos,
            BlockState state
    ) {
        this.level = level;
        this.targetBlock = state.getBlock();
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public boolean isConnected(int dx, int dy, int dz) {
        if (dx < -1 || dx > 1 || dy < -1 || dy > 1 || dz < -1 || dz > 1)
            throw new IllegalArgumentException("Offsets must be in the range [-1, 1]");

        int index = (dx + 1) + (dy + 1) * 3 + (dz + 1) * 9;
        int mask = 1 << index;
        if ((checkedMask & mask) != 0)
            return (connectedMask & mask) != 0;

        cursor.set(x + dx, y + dy, z + dz);
        BlockState state = level.getBlockState(cursor);
        boolean connected = state.is(targetBlock);
        checkedMask |= mask;
        if (connected) {
            connectedMask |= mask;
        }

        return connected;
    }

    public boolean isConnected(Direction direction) {
        return isConnected(direction.getStepX(), direction.getStepY(), direction.getStepZ());
    }

    public boolean isConnected(Direction first, Direction second) {
        return isConnected(
                first.getStepX() + second.getStepX(),
                first.getStepY() + second.getStepY(),
                first.getStepZ() + second.getStepZ()
        );
    }
}
