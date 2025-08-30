package dev.turtywurty.industria.multiblock.old;

import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

/**
 * Represents a multiblock structure that can be created and managed.
 * This interface provides methods to get the type of multiblock, its positions,
 * and handle events related to the multiblock's lifecycle.
 */
public interface Multiblockable {
    /**
     * Writes the multiblock positions to the given view.
     *
     * @param multiblockable The multiblockable instance to write positions from.
     * @param view           The view to write to.
     */
    static void write(AutoMultiblockable multiblockable, WriteView view) {
        view.put("MachinePositions", BlockPos.CODEC.listOf(), multiblockable.getMultiblockPositions());
    }

    /**
     * Reads the multiblock positions from the given view and clears the existing positions.
     *
     * @param multiblockable The multiblockable instance to read positions into.
     * @param view           The view to read from.
     */
    static void read(AutoMultiblockable multiblockable, ReadView view) {
        multiblockable.getMultiblockPositions().clear();
        view.read("MachinePositions", BlockPos.CODEC.listOf()).ifPresent(multiblockable.getMultiblockPositions()::addAll);
    }

    /**
     * Gets the type of multiblock this controller is.
     *
     * @return The type of multiblock this controller is.
     * @apiNote This method should return the same value every time it is called.
     * @see MultiblockType
     */
    MultiblockType<?> type();

    /**
     * Gets the positions of the blocks that make up the multiblock.
     *
     * @return A list of positions that make up the multiblock.
     * @apiNote This method should return a mutable list.
     */
    List<BlockPos> getMultiblockPositions();

    /**
     * Called when the multiblock is being broken.
     *
     * @param world The world the multiblock is being broken in.
     * @param pos   The position of the block that is being used to break the multiblock.
     * @apiNote This method should only be called on the server side.
     */
    void onMultiblockBreak(World world, BlockPos pos);

    /**
     * Gets the direction the multiblock is facing.
     *
     * @return The direction the multiblock is facing.
     * @apiNote This method should return the default direction if the multiblock does not have a direction property.
     */
    default Direction getFacing() {
        return Direction.NORTH;
    }
}
