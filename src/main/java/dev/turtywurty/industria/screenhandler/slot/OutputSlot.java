package dev.turtywurty.industria.screenhandler.slot;

import net.minecraft.inventory.SimpleInventory;

public class OutputSlot extends PredicateSlot {
    public OutputSlot(SimpleInventory inventory, int index, int x, int y) {
        super(inventory, index, x, y, itemStack -> false);
    }
}
