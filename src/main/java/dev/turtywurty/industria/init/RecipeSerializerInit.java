package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.recipe.*;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class RecipeSerializerInit {
    public static final RecipeSerializer<AlloyFurnaceRecipe> ALLOY_FURNACE =
            register("alloy_furnace", AlloyFurnaceRecipe.Serializer.INSTANCE);

    public static final RecipeSerializer<CrusherRecipe> CRUSHER =
            register("crusher", CrusherRecipe.Serializer.INSTANCE);

    public static final RecipeSerializer<UpgradeStationRecipe> UPGRADE_STATION =
            register("upgrade_station", UpgradeStationRecipe.Serializer.INSTANCE);

    public static final RecipeSerializer<MixerRecipe> MIXER =
            register("mixer", MixerRecipe.Serializer.INSTANCE);

    public static final RecipeSerializer<DigesterRecipe> DIGESTER =
            register("digester", DigesterRecipe.Serializer.INSTANCE);

    public static final RecipeSerializer<ClarifierRecipe> CLARIFIER =
            register("clarifier", ClarifierRecipe.Serializer.INSTANCE);

    public static final RecipeSerializer<CrystallizerRecipe> CRYSTALLIZER =
            register("crystallizer", CrystallizerRecipe.Serializer.INSTANCE);

    public static final RecipeSerializer<RotaryKilnRecipe> ROTARY_KILN =
            register("rotary_kiln", RotaryKilnRecipe.Serializer.INSTANCE);

    public static final RecipeSerializer<ElectrolyzerRecipe> ELECTROLYZER =
            register("electrolyzer", ElectrolyzerRecipe.Serializer.INSTANCE);

    public static final RecipeSerializer<ShakingTableRecipe> SHAKING_TABLE =
            register("shaking_table", ShakingTableRecipe.Serializer.INSTANCE);

    public static final RecipeSerializer<CentrifugalConcentratorRecipe> CENTRIFUGAL_CONCENTRATOR =
            register("centrifugal_concentrator", CentrifugalConcentratorRecipe.Serializer.INSTANCE);

    public static <T extends Recipe<?>> RecipeSerializer<T> register(String name, RecipeSerializer<T> serializer) {
        return Registry.register(Registries.RECIPE_SERIALIZER, Industria.id(name), serializer);
    }

    public static void init() {}
}
