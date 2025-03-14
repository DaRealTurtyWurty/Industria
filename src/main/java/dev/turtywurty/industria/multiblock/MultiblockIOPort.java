package dev.turtywurty.industria.multiblock;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public class MultiblockIOPort {
    private final Direction side;
    private final List<TransferType<?, ?>> transferTypes;

    public MultiblockIOPort(Direction side, List<TransferType<?, ?>> transferTypes) {
        this.side = side;
        this.transferTypes = transferTypes;
    }

    public MultiblockIOPort(Direction side, TransferType<?, ?>... transferTypes) {
        this.side = side;
        this.transferTypes = List.of(transferTypes);
    }

    public void tick(World world, BlockPos pos, BlockEntity controller) {
        for (TransferType<?, ?> transferType : this.transferTypes) {
            transferType.distribute(world, pos, controller, this.side);
        }
    }

    public <T> T getProvider(TransferType<T, ?> transferType, World world, BlockPos pos, BlockEntity controller) {
        return transferType.lookup(world, pos, controller.getCachedState(), controller, this.side);
    }

    public Direction getSide() {
        return this.side;
    }

    public List<TransferType<?, ?>> getTransferTypes() {
        return this.transferTypes;
    }
}
