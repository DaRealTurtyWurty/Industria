package dev.turtywurty.industria.recipe.input;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.recipe.CastingRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.Optional;

public record CastingRecipeInput(CastingRecipe.BlockType blockType, FluidStack fluidStack, Optional<Identifier> moldId)
        implements RecipeInput {
    @Override
    public ItemStack getItem(int slot) {
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
