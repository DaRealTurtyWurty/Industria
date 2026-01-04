package dev.turtywurty.industria.screenhandler.slot;

import net.minecraft.world.SimpleContainer;

public class OutputSlot extends PredicateSlot {
    public OutputSlot(SimpleContainer inventory, int index, int x, int y) {
        super(inventory, index, x, y, itemStack -> false);
    }
}
