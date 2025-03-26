package dev.turtywurty.industria.recipe.input;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public record CrystallizerRecipeInput(FluidStack waterFluid, FluidStack crystalFluid, ItemStack catalyst) implements RecipeInput {
    @Override
    public ItemStack getStackInSlot(int slot) {
        return slot == 0 ? catalyst : null;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return waterFluid.isEmpty() && crystalFluid.isEmpty() && catalyst.isEmpty();
    }
}
