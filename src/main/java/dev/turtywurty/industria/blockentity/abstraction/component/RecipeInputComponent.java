package dev.turtywurty.industria.blockentity.abstraction.component;

import dev.turtywurty.industria.blockentity.abstraction.IndustriaSimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public class RecipeInputComponent implements Component, RecipeInput {
    private final IndustriaSimpleInventory inventory;

    public RecipeInputComponent(IndustriaSimpleInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.inventory.getStack(slot);
    }

    @Override
    public int size() {
        return this.inventory.size();
    }
}
