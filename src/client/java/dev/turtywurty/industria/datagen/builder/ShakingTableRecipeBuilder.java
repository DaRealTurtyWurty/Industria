package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.recipe.ShakingTableRecipe;
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

public class ShakingTableRecipeBuilder implements RecipeBuilder {
    private final IndustriaIngredient input;
    private final OutputItemStack output;
    private final SlurryStack outputSlurry;
    private final int processTime;
    private final int frequency;

    private final RecipeCategory category;
    private final Map<String, Criterion<?>> criteria = new HashMap<>();

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
    public ShakingTableRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public ShakingTableRecipeBuilder group(@Nullable String group) {
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
                new ShakingTableRecipe(this.input, this.output, this.outputSlurry, this.processTime, this.frequency),
                builder.build(recipeKey.identifier().withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }
}
