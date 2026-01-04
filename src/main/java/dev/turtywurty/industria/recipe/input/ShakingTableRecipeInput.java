package dev.turtywurty.industria.recipe.input;

import dev.turtywurty.industria.blockentity.util.inventory.RecipeSimpleInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record ShakingTableRecipeInput(RecipeSimpleInventory recipeInventory, long waterAmount) implements RecipeInput {
    @Override
    public ItemStack getItem(int slot) {
        return this.recipeInventory.getItem(slot);
    }

    @Override
    public int size() {
        return this.recipeInventory.getContainerSize();
    }

    @Override
    public boolean isEmpty() {
        return this.recipeInventory.isEmpty();
    }
}
