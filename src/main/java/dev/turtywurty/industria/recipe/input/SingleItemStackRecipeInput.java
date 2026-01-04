package dev.turtywurty.industria.recipe.input;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record SingleItemStackRecipeInput(ItemStack stack) implements RecipeInput {
    public static SingleItemStackRecipeInput of(ItemStack stack) {
        return new SingleItemStackRecipeInput(stack);
    }

    @Override
    public ItemStack getItem(int slot) {
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
