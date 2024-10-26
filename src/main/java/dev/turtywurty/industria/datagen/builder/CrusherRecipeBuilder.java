package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.recipe.CrusherRecipe;
import dev.turtywurty.industria.util.IndustriaIngredient;
import dev.turtywurty.industria.util.OutputItemStack;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CrusherRecipeBuilder implements CraftingRecipeJsonBuilder {
    private final IndustriaIngredient input;
    private final OutputItemStack outputA, outputB;
    private final int processTime;

    private final RecipeCategory category;
    private final Map<String, AdvancementCriterion<?>> criteria = new HashMap<>();

    public CrusherRecipeBuilder(IndustriaIngredient input, OutputItemStack outputA, OutputItemStack outputB, int processTime, RecipeCategory category) {
        this.input = input;
        this.outputA = outputA;
        this.outputB = outputB;
        this.processTime = processTime;

        this.category = category;
    }

    @Override
    public CraftingRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public CraftingRecipeJsonBuilder group(@Nullable String group) {
        return this;
    }

    @Override
    public Item getOutputItem() {
        return this.outputA.item();
    }

    @Override
    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeId) {
        Advancement.Builder builder = exporter.getAdvancementBuilder()
                .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
                .rewards(AdvancementRewards.Builder.recipe(recipeId))
                .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        this.criteria.forEach(builder::criterion);
        exporter.accept(recipeId,
                new CrusherRecipe(this.input, this.outputA, this.outputB, this.processTime),
                builder.build(recipeId.getValue().withPrefixedPath("recipes/" + this.category.getName() + "/")));
    }
}
