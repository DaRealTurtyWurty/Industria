package dev.turtywurty.industria.block.abstraction;

import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorageHolder;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockEntityContentsDropper extends WrappedInventoryStorageHolder {
    Block getBlock();

    default void dropContents(World world, BlockPos pos) {
        WrappedInventoryStorage<?> wrappedStorage = getWrappedInventoryStorage();
        if (wrappedStorage != null) {
            wrappedStorage.dropContents(world, pos);

            Block block = getBlock();
            if (block != null) {
                world.updateComparators(pos, block);
            }
        }
    }
}
