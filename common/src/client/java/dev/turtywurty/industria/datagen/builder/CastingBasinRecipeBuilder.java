package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.recipe.CastingBasinRecipe;
import dev.turtywurty.industria.util.OutputItemStack;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public class CastingBasinRecipeBuilder {
    private final FluidStack input;
    private final OutputItemStack output;
    private final int coolingTime;

    public CastingBasinRecipeBuilder(FluidStack inputFluid, OutputItemStack output, int coolingTime) {
        this.input = inputFluid;
        this.output = output;
        this.coolingTime = coolingTime;
    }

    public void offerTo(RecipeOutput exporter, ResourceKey<Recipe<?>> recipeKey) {
        exporter.accept(recipeKey,
                new CastingBasinRecipe(this.input, this.output, this.coolingTime),
                null);
    }
}
