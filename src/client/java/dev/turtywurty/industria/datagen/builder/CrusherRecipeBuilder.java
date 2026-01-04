package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.recipe.CrusherRecipe;
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

import java.util.HashMap;
import java.util.Map;

public class CrusherRecipeBuilder implements RecipeBuilder {
    private final IndustriaIngredient input;
    private final OutputItemStack outputA, outputB;
    private final int processTime;

    private final RecipeCategory category;
    private final Map<String, Criterion<?>> criteria = new HashMap<>();

    public CrusherRecipeBuilder(IndustriaIngredient input, OutputItemStack outputA, OutputItemStack outputB, int processTime, RecipeCategory category) {
        this.input = input;
        this.outputA = outputA;
        this.outputB = outputB;
        this.processTime = processTime;

        this.category = category;
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public Item getResult() {
        return this.outputA.item();
    }

    @Override
    public void save(RecipeOutput exporter, ResourceKey<Recipe<?>> recipeId) {
        Advancement.Builder builder = exporter.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId))
                .rewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe(recipeId))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(builder::addCriterion);
        exporter.accept(recipeId,
                new CrusherRecipe(this.input, this.outputA, this.outputB, this.processTime),
                builder.build(recipeId.identifier().withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }
}
