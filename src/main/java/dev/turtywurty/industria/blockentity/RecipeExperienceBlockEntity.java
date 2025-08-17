package dev.turtywurty.industria.blockentity;

import com.mojang.serialization.Codec;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface RecipeExperienceBlockEntity {
    Codec<Map<RegistryKey<Recipe<?>>, Integer>> RECIPES_USED_CODEC =
            Codec.unboundedMap(RegistryKey.createCodec(RegistryKeys.RECIPE), Codec.INT);

    static void dropExperience(ServerWorld world, Vec3d pos, int multiplier, float experience) {
        int i = MathHelper.floor((float) multiplier * experience);
        float f = MathHelper.fractionalPart((float) multiplier * experience);
        if (f != 0.0F && Math.random() < (double) f) {
            i++;
        }

        ExperienceOrbEntity.spawn(world, pos, i);
    }

    static void writeRecipesUsed(WriteView view, String name, Reference2IntOpenHashMap<RegistryKey<Recipe<?>>> recipesUsed) {
        view.put(name, RECIPES_USED_CODEC, recipesUsed);
    }

    static void readRecipesUsed(ReadView view, String name, Reference2IntOpenHashMap<RegistryKey<Recipe<?>>> recipesUsed) {
        recipesUsed.clear();
        view.read(name, RECIPES_USED_CODEC).ifPresent(recipesUsed::putAll);
    }

    void setLastRecipe(@Nullable RecipeEntry<?> recipe);

    void dropExperienceForRecipesUsed(ServerPlayerEntity player);

    List<RecipeEntry<?>> getRecipesUsedAndDropExperience(ServerWorld world, Vec3d pos);
}