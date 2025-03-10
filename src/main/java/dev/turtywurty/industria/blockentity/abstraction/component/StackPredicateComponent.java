package dev.turtywurty.industria.blockentity.abstraction.component;

import net.minecraft.item.ItemStack;

import java.util.function.BiPredicate;

public class StackPredicateComponent implements Component {
    private final BiPredicate<Integer, ItemStack> predicate;

    public StackPredicateComponent(BiPredicate<Integer, ItemStack> predicate) {
        this.predicate = predicate;
    }

    public boolean test(int slot, ItemStack stack) {
        return this.predicate.test(slot, stack);
    }
}
