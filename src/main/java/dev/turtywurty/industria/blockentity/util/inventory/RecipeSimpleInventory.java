package dev.turtywurty.industria.blockentity.util.inventory;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public class RecipeSimpleInventory extends SimpleContainer implements RecipeInput {
    public RecipeSimpleInventory(int size) {
        super(size);
    }

    public RecipeSimpleInventory(ItemStack... stacks) {
        super(stacks);
    }

    @Override
    public ItemStack getItem(int slot) {
        return super.getItem(slot);
    }

    @Override
    public int size() {
        return getContainerSize();
    }
}
