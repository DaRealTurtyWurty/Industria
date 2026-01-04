package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.recipe.MixerRecipe;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MixerRecipeBuilder implements RecipeBuilder {
    private final List<IndustriaIngredient> inputs = new ArrayList<>();
    private final FluidStack inputFluid;
    private final int minTemperature, maxTemperature;
    private final OutputItemStack output;
    private final SlurryStack outputSlurry;
    private final int processTime;

    private final RecipeCategory category;
    private final Map<String, Criterion<?>> criteria = new HashMap<>();

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
    public MixerRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public MixerRecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public Item getResult() {
        return this.output.item();
    }

    @Override
    public void save(RecipeOutput exporter, ResourceKey<Recipe<?>> recipeKey) {
        Advancement.Builder builder = exporter.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeKey))
                .rewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe(recipeKey))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(builder::addCriterion);

        exporter.accept(recipeKey,
                new MixerRecipe(this.inputs, this.inputFluid, this.minTemperature, this.maxTemperature, this.output, this.outputSlurry, this.processTime),
                builder.build(recipeKey.identifier().withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }
}
