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

    public static final RecipeType<ClarifierRecipe> CLARIFIER =
            register("clarifier", ClarifierRecipe.Type.INSTANCE);

    public static final RecipeType<CrystallizerRecipe> CRYSTALLIZER =
            register("crystallizer", CrystallizerRecipe.Type.INSTANCE);

    public static final RecipeType<RotaryKilnRecipe> ROTARY_KILN =
            register("rotary_kiln", RotaryKilnRecipe.Type.INSTANCE);

    public static final RecipeType<ElectrolyzerRecipe> ELECTROLYZER =
            register("electrolyzer", ElectrolyzerRecipe.Type.INSTANCE);

    public static final RecipeType<ShakingTableRecipe> SHAKING_TABLE =
            register("shaking_table", ShakingTableRecipe.Type.INSTANCE);

    public static final RecipeType<CentrifugalConcentratorRecipe> CENTRIFUGAL_CONCENTRATOR =
            register("centrifugal_concentrator", CentrifugalConcentratorRecipe.Type.INSTANCE);

    public static <T extends Recipe<?>> RecipeType<T> register(String name, RecipeType<T> type) {
        return Registry.register(Registries.RECIPE_TYPE, Industria.id(name), type);
    }

    public static void init() {}
}
