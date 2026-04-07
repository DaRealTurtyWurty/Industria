package dev.turtywurty.industria.blockentity.util.inventory;

import dev.turtywurty.industria.blockentity.util.UpdateableBlockEntityLike;
import net.minecraft.world.item.ItemStack;

public class OutputSimpleInventory extends PredicateSimpleInventory {
    public OutputSimpleInventory(UpdateableBlockEntityLike blockEntity, int size) {
        super(blockEntity, size, (slot, stack) -> false);
    }

    public OutputSimpleInventory(UpdateableBlockEntityLike blockEntity, ItemStack... stacks) {
        super(blockEntity, (slot, stack) -> false, stacks);
    }
}
