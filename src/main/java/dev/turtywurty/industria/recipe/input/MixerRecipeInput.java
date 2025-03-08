package dev.turtywurty.industria.recipe.input;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.inventory.RecipeSimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public record MixerRecipeInput(RecipeSimpleInventory recipeInventory, FluidStack fluidStack, int temperature)
        implements RecipeInput {
    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.recipeInventory.getStackInSlot(slot);
    }

    @Override
    public int size() {
        return this.recipeInventory.size();
    }

    @Override
    public boolean isEmpty() {
        return this.recipeInventory.isEmpty();
    }
}
