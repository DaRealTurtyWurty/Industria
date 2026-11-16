package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.recipe.WireDrawplateRecipe;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public class WireDrawplateRecipeBuilder {
    private final IndustriaIngredient input;
    private final OutputItemStack output;
    private final int duration;

    public WireDrawplateRecipeBuilder(IndustriaIngredient input, OutputItemStack output, int duration) {
        this.input = input;
        this.output = output;
        this.duration = duration;
    }

    public void offerTo(RecipeOutput exporter, ResourceKey<Recipe<?>> recipeKey) {
        exporter.accept(recipeKey,
            new WireDrawplateRecipe(this.input, this.output, this.duration),
            null);
    }
}
