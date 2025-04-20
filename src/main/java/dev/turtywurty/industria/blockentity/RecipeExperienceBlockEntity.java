package dev.turtywurty.industria.blockentity;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface RecipeExperienceBlockEntity {
    void setLastRecipe(@Nullable RecipeEntry<?> recipe);

    void dropExperienceForRecipesUsed(ServerPlayerEntity player);

    List<RecipeEntry<?>> getRecipesUsedAndDropExperience(ServerWorld world, Vec3d pos);

    static void dropExperience(ServerWorld world, Vec3d pos, int multiplier, float experience) {
        int i = MathHelper.floor((float) multiplier * experience);
        float f = MathHelper.fractionalPart((float) multiplier * experience);
        if (f != 0.0F && Math.random() < (double) f) {
            i++;
        }

        ExperienceOrbEntity.spawn(world, pos, i);
    }

    static void writeRecipesUsed(Reference2IntOpenHashMap<RegistryKey<Recipe<?>>> recipesUsed, NbtCompound nbt, String name) {
        var recipesUsedNbt = new NbtCompound();
        recipesUsed.forEach((recipeRegistryKey, count) -> recipesUsedNbt.putInt(recipeRegistryKey.getValue().toString(), count));
        nbt.put(name, recipesUsedNbt);
    }

    static void readRecipesUsed(NbtCompound nbt, String name, Reference2IntOpenHashMap<RegistryKey<Recipe<?>>> recipesUsed) {
        if (nbt.contains(name)) {
            NbtCompound recipesUsedNbt = nbt.getCompoundOrEmpty(name);

            recipesUsed.clear();
            for (String key : recipesUsedNbt.getKeys()) {
                RegistryKey<Recipe<?>> recipeKey = RegistryKey.of(RegistryKeys.RECIPE, Identifier.of(key));
                recipesUsed.put(recipeKey, recipesUsedNbt.getInt(key, 0));
            }
        }
    }
}
