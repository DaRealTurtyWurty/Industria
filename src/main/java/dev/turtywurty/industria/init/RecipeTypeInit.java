package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.recipe.*;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class RecipeTypeInit {
    public static final RecipeType<AlloyFurnaceRecipe> ALLOY_FURNACE =
            register("alloy_furnace", AlloyFurnaceRecipe.Type.INSTANCE);

    public static final RecipeType<CrusherRecipe> CRUSHER =
            register("crusher", CrusherRecipe.Type.INSTANCE);

    public static final RecipeType<UpgradeStationRecipe> UPGRADE_STATION =
            register("upgrade_station", UpgradeStationRecipe.Type.INSTANCE);

    public static final RecipeType<MixerRecipe> MIXER =
            register("mixer", MixerRecipe.Type.INSTANCE);

    public static final RecipeType<DigesterRecipe> DIGESTER =
            register("digester", DigesterRecipe.Type.INSTANCE);

    public static <T extends Recipe<?>> RecipeType<T> register(String name, RecipeType<T> type) {
        return Registry.register(Registries.RECIPE_TYPE, Industria.id(name), type);
    }

    public static void init() {}
}
