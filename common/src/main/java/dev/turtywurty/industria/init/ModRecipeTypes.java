package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.recipe.*;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.function.Supplier;

public class ModRecipeTypes {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final RegistrationHandle<RecipeType<?>, RecipeType<AlloyingRecipe>> ALLOYING =
            register("alloying", () -> AlloyingRecipe.Type.INSTANCE);

    public static final RegistrationHandle<RecipeType<?>, RecipeType<CrusherRecipe>> CRUSHER =
            register("crusher", () -> CrusherRecipe.Type.INSTANCE);

    public static final RegistrationHandle<RecipeType<?>, RecipeType<UpgradeStationRecipe>> UPGRADE_STATION =
            register("upgrade_station", () -> UpgradeStationRecipe.Type.INSTANCE);

    public static final RegistrationHandle<RecipeType<?>, RecipeType<MixerRecipe>> MIXER =
            register("mixer", () -> MixerRecipe.Type.INSTANCE);

    public static final RegistrationHandle<RecipeType<?>, RecipeType<DigesterRecipe>> DIGESTER =
            register("digester", () -> DigesterRecipe.Type.INSTANCE);

    public static final RegistrationHandle<RecipeType<?>, RecipeType<ClarifierRecipe>> CLARIFIER =
            register("clarifier", () -> ClarifierRecipe.Type.INSTANCE);

    public static final RegistrationHandle<RecipeType<?>, RecipeType<CrystallizerRecipe>> CRYSTALLIZER =
            register("crystallizer", () -> CrystallizerRecipe.Type.INSTANCE);

    public static final RegistrationHandle<RecipeType<?>, RecipeType<RotaryKilnRecipe>> ROTARY_KILN =
            register("rotary_kiln", () -> RotaryKilnRecipe.Type.INSTANCE);

    public static final RegistrationHandle<RecipeType<?>, RecipeType<ElectrolyzerRecipe>> ELECTROLYZER =
            register("electrolyzer", () -> ElectrolyzerRecipe.Type.INSTANCE);

    public static final RegistrationHandle<RecipeType<?>, RecipeType<ShakingTableRecipe>> SHAKING_TABLE =
            register("shaking_table", () -> ShakingTableRecipe.Type.INSTANCE);

    public static final RegistrationHandle<RecipeType<?>, RecipeType<CentrifugalConcentratorRecipe>> CENTRIFUGAL_CONCENTRATOR =
            register("centrifugal_concentrator", () -> CentrifugalConcentratorRecipe.Type.INSTANCE);

    public static final RegistrationHandle<RecipeType<?>, RecipeType<RecyclingRecipe>> RECYCLING =
            register("recycling", () -> RecyclingRecipe.Type.INSTANCE);

    public static final RegistrationHandle<RecipeType<?>, RecipeType<AgitatorRecipe>> AGITATOR =
            register("agitator", () -> AgitatorRecipe.Type.INSTANCE);

    public static final RegistrationHandle<RecipeType<?>, RecipeType<DistillationTowerRecipe>> DISTILLATION_TOWER =
            register("distillation_tower", () -> DistillationTowerRecipe.Type.INSTANCE);

    public static <T extends Recipe<?>> RegistrationHandle<RecipeType<?>, RecipeType<T>> register(String name, Supplier<RecipeType<T>> type) {
        return REGISTRIES.registerRecipeType(Industria.id(name), type);
    }

    public static void init() {
    }
}
