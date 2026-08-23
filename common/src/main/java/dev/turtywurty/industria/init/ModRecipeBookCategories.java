package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.minecraft.world.item.crafting.RecipeBookCategory;

public class ModRecipeBookCategories {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final RegistrationHandle<RecipeBookCategory, RecipeBookCategory> ALLOYING = register("alloying");
    public static final RegistrationHandle<RecipeBookCategory, RecipeBookCategory> CRUSHER = register("crusher");
    public static final RegistrationHandle<RecipeBookCategory, RecipeBookCategory> UPGRADE_STATION = register("upgrade_station");
    public static final RegistrationHandle<RecipeBookCategory, RecipeBookCategory> MIXER = register("mixer");
    public static final RegistrationHandle<RecipeBookCategory, RecipeBookCategory> DIGESTER = register("digester");
    public static final RegistrationHandle<RecipeBookCategory, RecipeBookCategory> CLARIFIER = register("clarifier");
    public static final RegistrationHandle<RecipeBookCategory, RecipeBookCategory> CRYSTALLIZER = register("crystallizer");
    public static final RegistrationHandle<RecipeBookCategory, RecipeBookCategory> ROTARY_KILN = register("rotary_kiln");
    public static final RegistrationHandle<RecipeBookCategory, RecipeBookCategory> ELECTROLYZER = register("electrolyzer");
    public static final RegistrationHandle<RecipeBookCategory, RecipeBookCategory> SHAKING_TABLE = register("shaking_table");
    public static final RegistrationHandle<RecipeBookCategory, RecipeBookCategory> CENTRIFUGAL_CONCENTRATOR = register("centrifugal_concentrator");
    public static final RegistrationHandle<RecipeBookCategory, RecipeBookCategory> RECYCLING = register("recycling");
    public static final RegistrationHandle<RecipeBookCategory, RecipeBookCategory> AGITATOR = register("agitator");
    public static final RegistrationHandle<RecipeBookCategory, RecipeBookCategory> DISTILLATION_TOWER = register("distillation_tower");

    public static RegistrationHandle<RecipeBookCategory, RecipeBookCategory> register(String name) {
        return REGISTRIES.registerRecipeBookCategory(Industria.id(name), RecipeBookCategory::new);
    }

    public static void init() {
    }
}
