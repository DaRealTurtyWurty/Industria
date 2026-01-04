package dev.turtywurty.industria.blockentity.util.inventory;

import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import net.minecraft.world.item.ItemStack;

public class OutputSimpleInventory extends PredicateSimpleInventory {
    public OutputSimpleInventory(UpdatableBlockEntity blockEntity, int size) {
        super(blockEntity, size, (slot, stack) -> false);
    }

    public OutputSimpleInventory(UpdatableBlockEntity blockEntity, ItemStack... stacks) {
        super(blockEntity, (slot, stack) -> false, stacks);
    }
}
