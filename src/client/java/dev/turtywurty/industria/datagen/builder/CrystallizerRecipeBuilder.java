package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.recipe.CrystallizerRecipe;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;

public class CrystallizerRecipeBuilder {
    private final FluidStack waterFluid, crystalFluid;
    private final IndustriaIngredient catalyst;
    private final OutputItemStack output, byproduct;
    private final boolean requiresCatalyst;
    private final int catalystUses;
    private final int processTime;

    public CrystallizerRecipeBuilder(FluidStack waterFluid, FluidStack crystalFluid, IndustriaIngredient catalyst, OutputItemStack output, OutputItemStack byproduct, boolean requiresCatalyst, int catalystUses, int processTime) {
        this.waterFluid = waterFluid;
        this.crystalFluid = crystalFluid;
        this.catalyst = catalyst;
        this.output = output;
        this.byproduct = byproduct;
        this.requiresCatalyst = requiresCatalyst;
        this.catalystUses = catalystUses;
        this.processTime = processTime;
    }

    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
        exporter.accept(recipeKey,
                new CrystallizerRecipe(this.waterFluid, this.crystalFluid, this.catalyst, this.output, this.byproduct, this.requiresCatalyst, this.catalystUses, this.processTime),
                null);
    }
}
