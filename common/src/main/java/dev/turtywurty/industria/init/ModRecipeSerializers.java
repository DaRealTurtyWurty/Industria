package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.recipe.*;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Supplier;

public class ModRecipeSerializers {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final RegistrationHandle<RecipeSerializer<?>, RecipeSerializer<AlloyingRecipe>> ALLOYING =
            register("alloying", () -> AlloyingRecipe.SERIALIZER);

    public static final RegistrationHandle<RecipeSerializer<?>, RecipeSerializer<CrusherRecipe>> CRUSHER =
            register("crusher", () -> CrusherRecipe.SERIALIZER);

    public static final RegistrationHandle<RecipeSerializer<?>, RecipeSerializer<UpgradeStationRecipe>> UPGRADE_STATION =
            register("upgrade_station", () -> UpgradeStationRecipe.SERIALIZER);

    public static final RegistrationHandle<RecipeSerializer<?>, RecipeSerializer<MixerRecipe>> MIXER =
            register("mixer", () -> MixerRecipe.SERIALIZER);

    public static final RegistrationHandle<RecipeSerializer<?>, RecipeSerializer<DigesterRecipe>> DIGESTER =
            register("digester", () -> DigesterRecipe.SERIALIZER);

    public static final RegistrationHandle<RecipeSerializer<?>, RecipeSerializer<ClarifierRecipe>> CLARIFIER =
            register("clarifier", () -> ClarifierRecipe.SERIALIZER);

    public static final RegistrationHandle<RecipeSerializer<?>, RecipeSerializer<CrystallizerRecipe>> CRYSTALLIZER =
            register("crystallizer", () -> CrystallizerRecipe.SERIALIZER);

    public static final RegistrationHandle<RecipeSerializer<?>, RecipeSerializer<RotaryKilnRecipe>> ROTARY_KILN =
            register("rotary_kiln", () -> RotaryKilnRecipe.SERIALIZER);

    public static final RegistrationHandle<RecipeSerializer<?>, RecipeSerializer<ElectrolyzerRecipe>> ELECTROLYZER =
            register("electrolyzer", () -> ElectrolyzerRecipe.SERIALIZER);

    public static final RegistrationHandle<RecipeSerializer<?>, RecipeSerializer<ShakingTableRecipe>> SHAKING_TABLE =
            register("shaking_table", () -> ShakingTableRecipe.SERIALIZER);

    public static final RegistrationHandle<RecipeSerializer<?>, RecipeSerializer<CentrifugalConcentratorRecipe>> CENTRIFUGAL_CONCENTRATOR =
            register("centrifugal_concentrator", () -> CentrifugalConcentratorRecipe.SERIALIZER);

    public static final RegistrationHandle<RecipeSerializer<?>, RecipeSerializer<RecyclingRecipe>> RECYCLING =
            register("recycling", () -> RecyclingRecipe.SERIALIZER);

    public static final RegistrationHandle<RecipeSerializer<?>, RecipeSerializer<AgitatorRecipe>> AGITATOR =
            register("agitator", () -> AgitatorRecipe.SERIALIZER);

    public static final RegistrationHandle<RecipeSerializer<?>, RecipeSerializer<DistillationTowerRecipe>> DISTILLATION_TOWER =
            register("distillation_tower", () -> DistillationTowerRecipe.SERIALIZER);

    public static <T extends Recipe<?>> RegistrationHandle<RecipeSerializer<?>, RecipeSerializer<T>> register(String name, Supplier<RecipeSerializer<T>> serializer) {
        return REGISTRIES.registerRecipeSerializer(Industria.id(name), serializer);
    }

    public static void init() {
    }
}
