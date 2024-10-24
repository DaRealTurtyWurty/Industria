package dev.turtywurty.industria.datagen.builder;

import dev.turtywurty.industria.recipe.AlloyFurnaceRecipe;
import dev.turtywurty.industria.util.CountedIngredient;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AlloyFurnaceRecipeBuilder implements CraftingRecipeJsonBuilder {
    private final CountedIngredient inputA, inputB;
    private final ItemStack output;
    private final int smeltTime;

    private final RecipeCategory category;
    private final Map<String, AdvancementCriterion<?>> criteria = new HashMap<>();

    public AlloyFurnaceRecipeBuilder(CountedIngredient inputA, CountedIngredient inputB, ItemStack output, int smeltTime, RecipeCategory category) {
        this.inputA = inputA;
        this.inputB = inputB;
        this.output = output;
        this.smeltTime = smeltTime;
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
        return this.output.getItem();
    }

    @Override
    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> recipeId) {
        Advancement.Builder builder = exporter.getAdvancementBuilder()
                .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
                .rewards(AdvancementRewards.Builder.recipe(recipeId))
                .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        this.criteria.forEach(builder::criterion);
        exporter.accept(recipeId,
                new AlloyFurnaceRecipe(this.inputA, this.inputB, this.output, this.smeltTime),
                builder.build(recipeId.getValue().withPrefixedPath("recipes/" + this.category.getName() + "/")));
    }
}
