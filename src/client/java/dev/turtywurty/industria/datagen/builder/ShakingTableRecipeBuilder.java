package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.recipe.ShakingTableRecipe;
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

import java.util.HashMap;
import java.util.Map;

public class ShakingTableRecipeBuilder implements CraftingRecipeJsonBuilder {
    private final IndustriaIngredient input;
    private final OutputItemStack output;
    private final SlurryStack outputSlurry;
    private final int processTime;
    private final int frequency;

    private final RecipeCategory category;
    private final Map<String, AdvancementCriterion<?>> criteria = new HashMap<>();

    public ShakingTableRecipeBuilder(IndustriaIngredient input,
                                     OutputItemStack output, @Nullable SlurryStack outputSlurry,
                                     int processTime, int frequency,
                                     RecipeCategory category) {
        this.input = input;

        this.output = output;
        this.outputSlurry = outputSlurry;

        this.processTime = processTime;
        this.frequency = frequency;

        this.category = category;
    }

    @Override
    public ShakingTableRecipeBuilder criterion(String name, AdvancementCriterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public ShakingTableRecipeBuilder group(@Nullable String group) {
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
                new ShakingTableRecipe(this.input, this.output, this.outputSlurry, this.processTime, this.frequency),
                builder.build(recipeKey.getValue().withPrefixedPath("recipes/" + this.category.getName() + "/")));
    }
}
