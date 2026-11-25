package dev.turtywurty.industria.recipe.input;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.recipe.CastingRecipe;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.Optional;

public record CastingRecipeInput(
    CastingRecipe.BlockType blockType,
    FluidStack fluidStack,
    Optional<Identifier> moldId,
    ItemStack template
) implements RecipeInput {
    public CastingRecipeInput(CastingRecipe.BlockType blockType, FluidStack fluidStack, Optional<Identifier> moldId) {
        this(blockType, fluidStack, moldId, ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? this.template : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return fluidStack.isEmpty();
    }
}
