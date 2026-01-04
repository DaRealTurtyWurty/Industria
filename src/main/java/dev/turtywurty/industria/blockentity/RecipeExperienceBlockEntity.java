package dev.turtywurty.industria.blockentity;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface RecipeExperienceBlockEntity {
    Codec<Map<ResourceKey<Recipe<?>>, Integer>> RECIPES_USED_CODEC =
            Codec.unboundedMap(ResourceKey.codec(Registries.RECIPE), Codec.INT);

    static void dropExperience(ServerLevel world, Vec3 pos, int multiplier, float experience) {
        int i = Mth.floor((float) multiplier * experience);
        float f = Mth.frac((float) multiplier * experience);
        if (f != 0.0F && Math.random() < (double) f) {
            i++;
        }

        ExperienceOrb.award(world, pos, i);
    }

    static void writeRecipesUsed(ValueOutput view, String name, Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed) {
        view.store(name, RECIPES_USED_CODEC, recipesUsed);
    }

    static void readRecipesUsed(ValueInput view, String name, Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed) {
        recipesUsed.clear();
        view.read(name, RECIPES_USED_CODEC).ifPresent(recipesUsed::putAll);
    }

    void setLastRecipe(@Nullable RecipeHolder<?> recipe);

    void dropExperienceForRecipesUsed(ServerPlayer player);

    List<RecipeHolder<?>> getRecipesUsedAndDropExperience(ServerLevel world, Vec3 pos);
}