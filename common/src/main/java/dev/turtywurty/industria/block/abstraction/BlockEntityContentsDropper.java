package dev.turtywurty.industria.block.abstraction;

import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorageHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public interface BlockEntityContentsDropper extends WrappedContainerStorageHolder {
    Block getBlock();

    default void dropContents(Level world, BlockPos pos) {
        WrappedContainerStorage<?> wrappedStorage = getWrappedContainerStorage();
        if (wrappedStorage != null) {
            wrappedStorage.dropContents(world, pos);

            Block block = getBlock();
            if (block != null) {
                world.updateNeighbourForOutputSignal(pos, block);
            }
        }
    }
}
