package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.recipe.BlastFurnaceRecipe;
import dev.turtywurty.industria.util.IndustriaIngredient;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Recipe;

public class BlastFurnaceRecipeBuilder {
    private final IndustriaIngredient input;
    private final IndustriaIngredient flux;
    private final FluidStack output;
    private final ItemStackTemplate byproduct;
    private final int processTime;

    public BlastFurnaceRecipeBuilder(
            IndustriaIngredient input,
            IndustriaIngredient flux,
            FluidStack output,
            ItemStackTemplate byproduct,
            int processTime
    ) {
        this.input = input;
        this.flux = flux;
        this.output = output;
        this.byproduct = byproduct;
        this.processTime = processTime;
    }

    public void offerTo(RecipeOutput exporter, ResourceKey<Recipe<?>> recipeKey) {
        exporter.accept(recipeKey,
                new BlastFurnaceRecipe(input, flux, output, byproduct, processTime),
                null);
    }
}
