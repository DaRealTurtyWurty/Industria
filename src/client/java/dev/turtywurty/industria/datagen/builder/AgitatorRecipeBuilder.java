package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.recipe.AgitatorRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public class AgitatorRecipeBuilder {
    private final List<AgitatorRecipe.AgitatorInput> inputs;
    private final List<AgitatorRecipe.AgitatorOutput> outputs;
    private final int processTime;
    private final int energyCost;

    public AgitatorRecipeBuilder(List<AgitatorRecipe.AgitatorInput> inputs,
                                 List<AgitatorRecipe.AgitatorOutput> outputs,
                                 int processTime, int energyCost) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.processTime = processTime;
        this.energyCost = energyCost;
    }

    public void offerTo(RecipeOutput exporter, ResourceKey<Recipe<?>> recipeKey) {
        exporter.accept(recipeKey, new AgitatorRecipe(this.inputs, this.outputs, this.processTime, this.energyCost), null);
    }
}
