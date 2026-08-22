package dev.turtywurty.industria.recipe.input;

import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record DigesterRecipeInput(SlurryStack slurryStack) implements RecipeInput {
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
        return slurryStack.isEmpty();
    }
}
