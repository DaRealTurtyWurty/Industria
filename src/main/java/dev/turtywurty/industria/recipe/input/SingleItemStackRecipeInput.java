package dev.turtywurty.industria.recipe.input;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public record SingleItemStackRecipeInput(ItemStack stack) implements RecipeInput {
    public static SingleItemStackRecipeInput of(ItemStack stack) {
        return new SingleItemStackRecipeInput(stack);
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot == 0 ? this.stack : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.stack.isEmpty();
    }
}
