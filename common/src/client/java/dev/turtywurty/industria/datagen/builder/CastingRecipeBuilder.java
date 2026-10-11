package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.recipe.CastingRecipe;
import dev.turtywurty.industria.util.OutputItemStack;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Optional;

public class CastingRecipeBuilder {
    private final CastingRecipe.BlockType blockType;
    private final FluidStack input;
    private final OutputItemStack output;
    private final int coolingTime;
    private Optional<Identifier> moldId = Optional.empty();

    public CastingRecipeBuilder(CastingRecipe.BlockType blockType, FluidStack inputFluid, OutputItemStack output, int coolingTime) {
        this.blockType = blockType;
        this.input = inputFluid;
        this.output = output;
        this.coolingTime = coolingTime;
    }

    public CastingRecipeBuilder mold(Identifier id) {
        this.moldId = Optional.of(id);
        return this;
    }

    public void offerTo(RecipeOutput exporter, ResourceKey<Recipe<?>> recipeKey) {
        exporter.accept(recipeKey,
                new CastingRecipe(this.blockType, this.input, this.output, this.coolingTime, this.moldId),
                null);
    }
}
