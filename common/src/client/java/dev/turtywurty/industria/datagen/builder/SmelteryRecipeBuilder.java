package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.recipe.SmelteryRecipe;
import dev.turtywurty.industria.util.IndustriaIngredient;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public class SmelteryRecipeBuilder {
    private final IndustriaIngredient input;
    private final FluidStack output;
    private final int preferredTemperature;
    private final float maxSpeedMultiplier;
    private final int processTime;

    public SmelteryRecipeBuilder(IndustriaIngredient input, FluidStack output, int preferredTemperature,
                                 float maxSpeedMultiplier, int processTime) {
        this.input = input;
        this.output = output;
        this.preferredTemperature = preferredTemperature;
        this.maxSpeedMultiplier = maxSpeedMultiplier;
        this.processTime = processTime;
    }

    public void offerTo(RecipeOutput exporter, ResourceKey<Recipe<?>> recipeKey) {
        exporter.accept(recipeKey,
                new SmelteryRecipe(this.input, this.output, this.preferredTemperature, this.maxSpeedMultiplier,
                        this.processTime),
                null);
    }
}
