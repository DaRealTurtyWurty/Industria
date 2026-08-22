package dev.turtywurty.industria.recipe.input;

import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record ElectrolyzerRecipeInput(SimpleContainer inputInventory, SimpleContainer anodeInventory,
                                      SimpleContainer cathodeInventory, SimpleContainer electrolyteItemInventory,
                                      SyncingFluidStorage electrolyteFluidStorage) implements RecipeInput {
    @Override
    public ItemStack getItem(int slot) {
        return switch (slot) {
            case 0 -> inputInventory.getItem(0);
            case 1 -> anodeInventory.getItem(0);
            case 2 -> cathodeInventory.getItem(0);
            case 3 -> electrolyteItemInventory.getItem(0);
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        return inputInventory.isEmpty() &&
                anodeInventory.isEmpty() && cathodeInventory.isEmpty() &&
                electrolyteItemInventory.isEmpty() &&
                (electrolyteFluidStorage.getResource().isBlank() || electrolyteFluidStorage.getAmount() <= 0);
    }
}
