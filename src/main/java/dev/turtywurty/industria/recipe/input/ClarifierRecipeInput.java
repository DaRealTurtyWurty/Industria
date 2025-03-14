package dev.turtywurty.industria.recipe.input;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public record ClarifierRecipeInput(FluidStack fluidStack) implements RecipeInput {
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
        return fluidStack.isEmpty();
    }
}
