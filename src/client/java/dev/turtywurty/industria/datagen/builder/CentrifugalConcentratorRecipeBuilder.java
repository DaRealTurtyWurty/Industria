package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.recipe.CentrifugalConcentratorRecipe;
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

public class CentrifugalConcentratorRecipeBuilder implements RecipeBuilder {
    private final IndustriaIngredient input;
    private final OutputItemStack output;
    private final SlurryStack outputSlurry;
    private final int processTime;
    private final int rpm;

    private final RecipeCategory category;
    private final Map<String, Criterion<?>> criteria = new HashMap<>();

    public CentrifugalConcentratorRecipeBuilder(IndustriaIngredient input,
                                                OutputItemStack output, @Nullable SlurryStack outputSlurry,
                                                int processTime, int rpm,
                                                RecipeCategory category) {
        this.input = input;

        this.output = output;
        this.outputSlurry = outputSlurry;

        this.processTime = processTime;
        this.rpm = rpm;

        this.category = category;
    }

    @Override
    public CentrifugalConcentratorRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public CentrifugalConcentratorRecipeBuilder group(@Nullable String group) {
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
                new CentrifugalConcentratorRecipe(this.input, this.output, this.outputSlurry, this.processTime, this.rpm),
                builder.build(recipeKey.identifier().withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }
}
