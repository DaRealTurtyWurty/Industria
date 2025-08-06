package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class RecipeBookCategoryInit {
    public static final RecipeBookCategory ALLOY_FURNACE = register("alloy_furnace");
    public static final RecipeBookCategory CRUSHER = register("crusher");
    public static final RecipeBookCategory UPGRADE_STATION = register("upgrade_station");
    public static final RecipeBookCategory MIXER = register("mixer");
    public static final RecipeBookCategory DIGESTER = register("digester");
    public static final RecipeBookCategory CLARIFIER = register("clarifier");
    public static final RecipeBookCategory CRYSTALLIZER = register("crystallizer");
    public static final RecipeBookCategory ROTARY_KILN = register("rotary_kiln");
    public static final RecipeBookCategory ELECTROLYZER = register("electrolyzer");
    public static final RecipeBookCategory SHAKING_TABLE = register("shaking_table");

    public static RecipeBookCategory register(String name) {
        return Registry.register(Registries.RECIPE_BOOK_CATEGORY, Industria.id(name), new RecipeBookCategory());
    }

    public static void init() {}
}
