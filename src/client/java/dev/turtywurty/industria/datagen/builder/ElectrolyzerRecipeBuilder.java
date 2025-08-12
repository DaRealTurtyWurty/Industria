package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.gas.GasStack;
import dev.turtywurty.industria.recipe.ElectrolyzerRecipe;
import dev.turtywurty.industria.util.IndustriaIngredient;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.recipe.Recipe;
import net.minecraft.registry.RegistryKey;

public class ElectrolyzerRecipeBuilder {
    private final IndustriaIngredient input, anode, cathode, electrolyteItem;
    private final FluidStack electrolyteFluid, outputFluid;
    private final GasStack outputGas;
    private final int processTime, energyCost, temperature;

    public ElectrolyzerRecipeBuilder(IndustriaIngredient input, IndustriaIngredient anode, IndustriaIngredient cathode,
                                     IndustriaIngredient electrolyteItem, FluidStack electrolyteFluid,
                                     FluidStack outputFluid, GasStack outputGas,
                                     int processTime, int energyCost, int temperature) {
        this.input = input;
        this.anode = anode;
        this.cathode = cathode;
        this.electrolyteItem = electrolyteItem;
        this.electrolyteFluid = electrolyteFluid;
        this.outputFluid = outputFluid;
        this.outputGas = outputGas;
        this.processTime = processTime;
        this.energyCost = energyCost;
        this.temperature = temperature;
    }

    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
        exporter.accept(recipeKey,
                new ElectrolyzerRecipe(this.input, this.anode, this.cathode, this.electrolyteItem,
                        this.electrolyteFluid, this.outputFluid, this.outputGas,
                        this.processTime, this.energyCost, this.temperature),
                null);
    }
}
