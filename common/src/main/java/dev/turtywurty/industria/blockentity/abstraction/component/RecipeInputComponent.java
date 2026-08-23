package dev.turtywurty.industria.blockentity.abstraction.component;

import dev.turtywurty.industria.blockentity.abstraction.IndustriaSimpleInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public class RecipeInputComponent implements Component, RecipeInput {
    private final IndustriaSimpleInventory inventory;

    public RecipeInputComponent(IndustriaSimpleInventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.inventory.getItem(slot);
    }

    @Override
    public int size() {
        return this.inventory.getContainerSize();
    }
}
