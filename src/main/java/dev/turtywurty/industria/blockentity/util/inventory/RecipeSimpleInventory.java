package dev.turtywurty.industria.blockentity.util.inventory;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public class RecipeSimpleInventory extends SimpleInventory implements RecipeInput {
    public RecipeSimpleInventory(int size) {
        super(size);
    }

    public RecipeSimpleInventory(ItemStack... stacks) {
        super(stacks);
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return getStack(slot);
    }
}
