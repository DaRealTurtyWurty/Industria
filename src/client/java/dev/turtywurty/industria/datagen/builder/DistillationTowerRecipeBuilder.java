package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.recipe.DistillationTowerRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;

public class DistillationTowerRecipeBuilder {
    private final FluidStack inputFluid;
    private final FluidStack primaryOutputFluid;
    private final FluidStack secondaryOutputFluid;
    private final int processTime;
    private final int energyCost;

    public DistillationTowerRecipeBuilder(@NotNull FluidStack inputFluid, @NotNull FluidStack primaryOutputFluid,
                                          @NotNull FluidStack secondaryOutputFluid, int processTime, int energyCost) {
        this.inputFluid = inputFluid;
        this.primaryOutputFluid = primaryOutputFluid;
        this.secondaryOutputFluid = secondaryOutputFluid;
        this.processTime = processTime;
        this.energyCost = energyCost;
    }

    public void offerTo(RecipeOutput exporter, ResourceKey<Recipe<?>> recipeKey) {
        exporter.accept(recipeKey,
                new DistillationTowerRecipe(this.inputFluid, this.primaryOutputFluid, this.secondaryOutputFluid, this.processTime, this.energyCost),
                null);
    }
}
