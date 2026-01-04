package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.recipe.RotaryKilnRecipe;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public class RotaryKilnRecipeBuilder {
    private final IndustriaIngredient input;
    private final OutputItemStack output;
    private final int requiredTemperature;

    public RotaryKilnRecipeBuilder(IndustriaIngredient input, OutputItemStack output, int requiredTemperature) {
        this.input = input;
        this.output = output;
        this.requiredTemperature = requiredTemperature;
    }

    public void offerTo(RecipeOutput exporter, ResourceKey<Recipe<?>> recipeKey) {
        exporter.accept(recipeKey,
                new RotaryKilnRecipe(this.input, this.output, this.requiredTemperature),
                null);
    }
}
