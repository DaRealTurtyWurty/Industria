package dev.turtywurty.industria.recipe.input;

import dev.turtywurty.industria.util.AgitatorPortType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public record AgitatorRecipeInput(List<AgitatorPortStack> inputs) implements RecipeInput {
    public static final int INPUT_COUNT = 3;

    public AgitatorRecipeInput {
        inputs = List.copyOf(inputs);

        if (inputs.size() != INPUT_COUNT)
            throw new IllegalArgumentException("Agitator recipes require exactly " + INPUT_COUNT + " inputs");
    }

    public AgitatorPortStack getPort(int index) {
        return this.inputs.get(index);
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= this.inputs.size())
            return ItemStack.EMPTY;

        AgitatorPortStack stack = this.inputs.get(slot);
        return stack.type() == AgitatorPortType.ITEM ? stack.item() : ItemStack.EMPTY;
    }

    @Override
    public int size() {
        return this.inputs.size();
    }

    @Override
    public boolean isEmpty() {
        return this.inputs.stream().allMatch(AgitatorPortStack::isEmpty);
    }
}
