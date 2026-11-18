package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.recipe.CokeOvenRecipe;
import dev.turtywurty.industria.util.IndustriaIngredient;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;

public class CokeOvenRecipeBuilder {
    private final IndustriaIngredient input;
    private final ItemStackTemplate outputItem;
    private final FluidStack outputFluid;
    private final int processTime;
    private final int requiredTemperature;

    public CokeOvenRecipeBuilder(
            IndustriaIngredient input,
            ItemStackTemplate outputItem,
            FluidStack outputFluid,
            int processTime,
            int requiredTemperature
    ) {
        this.input = input;
        this.outputItem = outputItem;
        this.outputFluid = outputFluid;
        this.processTime = processTime;
        this.requiredTemperature = requiredTemperature;
    }

    public void offerTo(RecipeOutput exporter, ResourceKey<Recipe<?>> recipeKey) {
        exporter.accept(recipeKey,
                new CokeOvenRecipe(input, outputItem, outputFluid, requiredTemperature, processTime),
                null);
    }
}
