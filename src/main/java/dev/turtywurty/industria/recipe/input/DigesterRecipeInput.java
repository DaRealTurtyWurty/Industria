package dev.turtywurty.industria.recipe.input;

import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public record DigesterRecipeInput(SlurryStack slurryStack) implements RecipeInput {
    @Override
    public ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return slurryStack.isEmpty();
    }
}
