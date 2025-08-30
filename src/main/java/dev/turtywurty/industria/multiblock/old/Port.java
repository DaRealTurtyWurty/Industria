package dev.turtywurty.industria.multiblock.old;

import dev.turtywurty.industria.multiblock.PortType;
import dev.turtywurty.industria.multiblock.TransferType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

/**
 * Represents a port on a multiblock structure.
 * A port can have multiple PortTypes, which define the type of transfer that can occur through
 * this port, such as energy, fluids, or items.
 *
 * @param side      the side of the multiblock structure this port is on
 * @param portTypes the list of PortTypes that this port supports
 */
public record Port(Direction side, List<PortType> portTypes) {
    /**
     * Creates a new Port with the specified side and a list of PortTypes.
     *
     * @param side          the side of the multiblock structure this port is on
     * @param transferTypes the list of PortTypes that this port supports
     */
    public Port(Direction side, PortType... transferTypes) {
        this(side, List.of(transferTypes));
    }

    /**
     * Ticks the port, allowing it to perform any necessary updates.
     *
     * @param world      the world in which the port exists
     * @param pos        the position of the port in the world
     * @param controller the position of the multiblock controller
     */
    public void tick(World world, BlockPos pos, BlockPos controller) {
        for (PortType portType : this.portTypes) {
            if (portType.isOutput()) {
                portType.transferType().pushTo(world, controller, pos.offset(this.side), this.side);
            }
        }
    }

    /**
     * Retrieves a provider for the specified transfer type at the given position in the world.
     * <p>
     * TODO: Maybe handle PortType#isInput here somewhere?
     *
     * @param transferType the type of transfer to look up
     * @param world        the world in which the port exists
     * @param pos          the position of the port in the world
     * @param controller   the BlockEntity representing the multiblock controller
     * @return an instance of the provider for the specified transfer type

     */
    public <T> T getProvider(TransferType<T, ?, ?> transferType, World world, BlockPos pos, BlockEntity controller) {
        return transferType.lookup(world, pos, controller.getCachedState(), controller, this.side);
    }
}
