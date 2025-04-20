package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.recipe.DigesterRecipe;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;
import org.jetbrains.annotations.NotNull;

public class DigesterRecipeBuilder {
    private final SlurryStack inputSlurry;
    private final FluidStack outputFluid;
    private final int processTime;

    public DigesterRecipeBuilder(@NotNull SlurryStack inputSlurry, @NotNull FluidStack outputFluid, int processTime) {
        this.inputSlurry = inputSlurry;
        this.outputFluid = outputFluid;
        this.processTime = processTime;
    }

    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
        exporter.accept(recipeKey,
                new DigesterRecipe(this.inputSlurry, this.outputFluid, this.processTime),
                null);
    }
}