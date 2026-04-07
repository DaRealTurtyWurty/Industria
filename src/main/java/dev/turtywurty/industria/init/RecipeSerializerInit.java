package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.recipe.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RecipeSerializerInit {
    public static final RecipeSerializer<AlloyFurnaceRecipe> ALLOY_FURNACE =
            register("alloy_furnace", AlloyFurnaceRecipe.SERIALIZER);

    public static final RecipeSerializer<CrusherRecipe> CRUSHER =
            register("crusher", CrusherRecipe.SERIALIZER);

    public static final RecipeSerializer<UpgradeStationRecipe> UPGRADE_STATION =
            register("upgrade_station", UpgradeStationRecipe.SERIALIZER);

    public static final RecipeSerializer<MixerRecipe> MIXER =
            register("mixer", MixerRecipe.SERIALIZER);

    public static final RecipeSerializer<DigesterRecipe> DIGESTER =
            register("digester", DigesterRecipe.SERIALIZER);

    public static final RecipeSerializer<ClarifierRecipe> CLARIFIER =
            register("clarifier", ClarifierRecipe.SERIALIZER);

    public static final RecipeSerializer<CrystallizerRecipe> CRYSTALLIZER =
            register("crystallizer", CrystallizerRecipe.SERIALIZER);

    public static final RecipeSerializer<RotaryKilnRecipe> ROTARY_KILN =
            register("rotary_kiln", RotaryKilnRecipe.SERIALIZER);

    public static final RecipeSerializer<ElectrolyzerRecipe> ELECTROLYZER =
            register("electrolyzer", ElectrolyzerRecipe.SERIALIZER);

    public static final RecipeSerializer<ShakingTableRecipe> SHAKING_TABLE =
            register("shaking_table", ShakingTableRecipe.SERIALIZER);

    public static final RecipeSerializer<CentrifugalConcentratorRecipe> CENTRIFUGAL_CONCENTRATOR =
            register("centrifugal_concentrator", CentrifugalConcentratorRecipe.SERIALIZER);

    public static final RecipeSerializer<RecyclingRecipe> RECYCLING =
            register("recycling", RecyclingRecipe.SERIALIZER);

    public static <T extends Recipe<?>> RecipeSerializer<T> register(String name, RecipeSerializer<T> serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Industria.id(name), serializer);
    }

    public static void init() {}
}
