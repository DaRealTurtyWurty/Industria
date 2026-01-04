package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.recipe.ClarifierRecipe;
import dev.turtywurty.industria.util.OutputItemStack;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

public class ClarifierRecipeBuilder {
    private final FluidStack inputFluid;
    private final FluidStack outputFluid;
    private final OutputItemStack outputItem;
    private final int processTime;

    public ClarifierRecipeBuilder(@NotNull FluidStack inputFluid, @NotNull FluidStack outputFluid, @NotNull OutputItemStack outputItem, int processTime) {
        this.inputFluid = inputFluid;
        this.outputFluid = outputFluid;
        this.outputItem = outputItem;
        this.processTime = processTime;
    }

    public void offerTo(RecipeOutput exporter, ResourceKey<Recipe<?>> recipeKey) {
        exporter.accept(recipeKey,
                new ClarifierRecipe(this.inputFluid, this.outputFluid, this.outputItem, this.processTime),
                null);
    }
}
