package dev.turtywurty.industria.blockentity;

import dev.turtywurty.industria.util.ViewUtils;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface RecipeExperienceBlockEntity {
    static void dropExperience(ServerWorld world, Vec3d pos, int multiplier, float experience) {
        int i = MathHelper.floor((float) multiplier * experience);
        float f = MathHelper.fractionalPart((float) multiplier * experience);
        if (f != 0.0F && Math.random() < (double) f) {
            i++;
        }

        ExperienceOrbEntity.spawn(world, pos, i);
    }

    static void writeRecipesUsed(WriteView view, String name, Reference2IntOpenHashMap<RegistryKey<Recipe<?>>> recipesUsed) {
        var recipesUsedView = view.get(name);
        recipesUsed.forEach((recipeRegistryKey, count) -> recipesUsedView.putInt(recipeRegistryKey.getValue().toString(), count));
    }

    static void readRecipesUsed(ReadView view, String name, Reference2IntOpenHashMap<RegistryKey<Recipe<?>>> recipesUsed) {
        ReadView recipesUsedView = view.getReadView(name);

        recipesUsed.clear();
        for (String key : ViewUtils.getKeys(recipesUsedView)) {
            RegistryKey<Recipe<?>> recipeKey = RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(key));
            recipesUsed.put(recipeKey, recipesUsedView.getInt(key, 0));
        }
    }

    void setLastRecipe(@Nullable RecipeEntry<?> recipe);

    void dropExperienceForRecipesUsed(ServerPlayerEntity player);

    List<RecipeEntry<?>> getRecipesUsedAndDropExperience(ServerWorld world, Vec3d pos);
}
