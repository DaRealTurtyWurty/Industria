package dev.turtywurty.industria.screenhandler.slot;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

public class PredicateSlot extends Slot {
    private final Predicate<ItemStack> predicate;

    public PredicateSlot(SimpleContainer inventory, int index, int x, int y, Predicate<ItemStack> predicate) {
        super(inventory, index, x, y);
        this.predicate = predicate;
    }

    public PredicateSlot(SimpleContainer inventory, int index, int x, int y) {
        this(inventory, index, x, y, stack -> inventory.canPlaceItem(index, stack));
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return this.predicate.test(stack);
    }
}
