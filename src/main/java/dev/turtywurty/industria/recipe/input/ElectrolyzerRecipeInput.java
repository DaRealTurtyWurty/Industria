package dev.turtywurty.industria.recipe.input;

import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record ElectrolyzerRecipeInput(SimpleContainer inputInventory, SimpleContainer anodeInventory,
                                      SimpleContainer cathodeInventory, SimpleContainer electrolyteItemInventory,
                                      SingleFluidStorage electrolyteFluidStorage) implements RecipeInput {
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
                (electrolyteFluidStorage.isResourceBlank() || electrolyteFluidStorage.getAmount() <= 0);
    }
}
