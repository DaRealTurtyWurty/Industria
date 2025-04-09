package dev.turtywurty.industria.recipe.input;

import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public record ElectrolyzerRecipeInput(SimpleInventory inputInventory, SimpleInventory anodeInventory,
                                      SimpleInventory cathodeInventory, SimpleInventory electrolyteItemInventory,
                                      SingleFluidStorage electrolyteFluidStorage) implements RecipeInput {
    @Override
    public ItemStack getStackInSlot(int slot) {
        return switch (slot) {
            case 0 -> inputInventory.getStack(0);
            case 1 -> anodeInventory.getStack(0);
            case 2 -> cathodeInventory.getStack(0);
            case 3 -> electrolyteItemInventory.getStack(0);
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
