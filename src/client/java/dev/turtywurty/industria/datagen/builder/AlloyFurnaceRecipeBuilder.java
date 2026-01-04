package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.recipe.AlloyFurnaceRecipe;
import dev.turtywurty.industria.util.IndustriaIngredient;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AlloyFurnaceRecipeBuilder implements RecipeBuilder {
    private final IndustriaIngredient inputA, inputB;
    private final ItemStack output;
    private final int smeltTime;

    private final RecipeCategory category;
    private final Map<String, Criterion<?>> criteria = new HashMap<>();

    public AlloyFurnaceRecipeBuilder(IndustriaIngredient inputA, IndustriaIngredient inputB, ItemStack output, int smeltTime, RecipeCategory category) {
        this.inputA = inputA;
        this.inputB = inputB;
        this.output = output;
        this.smeltTime = smeltTime;
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
        return this.output.getItem();
    }

    @Override
    public void save(RecipeOutput exporter, ResourceKey<Recipe<?>> recipeId) {
        Advancement.Builder builder = exporter.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(recipeId))
                .rewards(net.minecraft.advancements.AdvancementRewards.Builder.recipe(recipeId))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(builder::addCriterion);
        exporter.accept(recipeId,
                new AlloyFurnaceRecipe(this.inputA, this.inputB, this.output, this.smeltTime),
                builder.build(recipeId.identifier().withPrefix("recipes/" + this.category.getFolderName() + "/")));
    }
}
