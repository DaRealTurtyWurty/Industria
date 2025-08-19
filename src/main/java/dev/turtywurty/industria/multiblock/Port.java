package dev.turtywurty.industria.multiblock;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public record Port(Direction side, List<PortType> portTypes) {
    public Port(Direction side, PortType... transferTypes) {
        this(side, List.of(transferTypes));
    }

    public void tick(World world, BlockPos pos, BlockPos controller) {
        for (PortType portType : this.portTypes) {
            if(portType.isOutput()) {
                portType.transferType().pushTo(world, controller, pos.offset(this.side), this.side);
            }
        }
    }

    // TODO: Maybe handle PortType#isInput here somewhere?
    public <T> T getProvider(TransferType<T, ?, ?> transferType, World world, BlockPos pos, BlockEntity controller) {
        return transferType.lookup(world, pos, controller.getCachedState(), controller, this.side);
    }
}
