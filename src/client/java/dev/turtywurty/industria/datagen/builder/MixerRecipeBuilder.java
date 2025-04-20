package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.recipe.MixerRecipe;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MixerRecipeBuilder implements CraftingRecipeJsonBuilder {
    private final List<IndustriaIngredient> inputs = new ArrayList<>();
    private final FluidStack inputFluid;
    private final int minTemperature, maxTemperature;
    private final OutputItemStack output;
    private final SlurryStack outputSlurry;
    private final int processTime;

    private final RecipeCategory category;
    private final Map<String, AdvancementCriterion<?>> criteria = new HashMap<>();

    public MixerRecipeBuilder(Collection<IndustriaIngredient> inputs, @Nullable FluidStack inputFluid,
                              int minTemperature, int maxTemperature,
                              OutputItemStack output, @Nullable SlurryStack outputSlurry,
                              int processTime,
                              RecipeCategory category) {
        this.inputs.addAll(inputs);
        this.inputFluid = inputFluid;

        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;

        this.output = output;
        this.outputSlurry = outputSlurry;

        this.processTime = processTime;

        this.category = category;
    }

    @Override
    public MixerRecipeBuilder criterion(String name, AdvancementCriterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public MixerRecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public Item getOutputItem() {
        return this.output.item();
    }

    @Override
    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeKey) {
        Advancement.Builder builder = exporter.getAdvancementBuilder()
                .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeKey))
                .rewards(AdvancementRewards.Builder.recipe(recipeKey))
                .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        this.criteria.forEach(builder::criterion);

        exporter.accept(recipeKey,
                new MixerRecipe(this.inputs, this.inputFluid, this.minTemperature, this.maxTemperature, this.output, this.outputSlurry, this.processTime),
                builder.build(recipeKey.getValue().withPrefixedPath("recipes/" + this.category.getName() + "/")));
    }
}
