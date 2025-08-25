package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.item.*;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.function.Function;

public class ItemInit {
    public static final SeismicScannerItem SEISMIC_SCANNER = register("seismic_scanner",
            SeismicScannerItem::new, settings -> settings.maxCount(1));

    public static final SimpleDrillHeadItem SIMPLE_DRILL_HEAD = register("simple_drill_head",
            SimpleDrillHeadItem::new, settings -> settings.maxCount(1));

    public static final BlockBuilderDrillHeadItem BLOCK_BUILDER_DRILL_HEAD = register("block_builder_drill_head",
            BlockBuilderDrillHeadItem::new, settings -> settings.maxCount(1));

    public static final RotaryKilnBlockItem ROTARY_KILN = register("rotary_kiln", RotaryKilnBlockItem::new);

    // Aluminium
    public static final Item BAUXITE = register("bauxite");
    public static final Item CRUSHED_BAUXITE = register("crushed_bauxite");
    public static final Item SODIUM_ALUMINATE = register("sodium_aluminate");
    public static final Item ALUMINIUM_HYDROXIDE = register("aluminium_hydroxide");
    public static final Item ALUMINA = register("alumina");
    public static final Item ALUMINIUM_INGOT = register("aluminium_ingot");
    public static final Item ALUMINIUM_NUGGET = register("aluminium_nugget");
    public static final Item ALUMINIUM_PLATE = register("aluminium_plate");

    // Silver
    public static final Item ARGENTITE = register("argentite");
    public static final Item CRUSHED_ARGENTITE = register("crushed_argentite");
    public static final Item ARGENTITE_CONCENTRATE = register("argentite_concentrate");
    public static final Item LEAD_BULLION = register("lead_bullion");
    public static final Item DORE_SILVER = register("dore_silver");
    public static final Item SILVER_INGOT = register("silver_ingot");
    public static final Item SILVER_NUGGET = register("silver_nugget");

    // Lead
    public static final Item GALENA = register("galena");
    public static final Item CRUSHED_GALENA = register("crushed_galena");
    public static final Item GALENA_CONCENTRATE = register("galena_concentrate");
    public static final Item TETRAGONAL_LITHARGE = register("tetragonal_litharge");
    public static final Item LEAD_INGOT = register("lead_ingot");
    public static final Item LEAD_NUGGET = register("lead_nugget");

    // Titanium
    public static final Item ILMENITE = register("ilmenite");
    public static final Item CRUSHED_ILMENITE = register("crushed_ilmenite");
    public static final Item ILMENITE_CONCENTRATE = register("ilmenite_concentrate");
    public static final Item TITANIUM_TETRACHLORIDE = register("titanium_tetrachloride");
    public static final Item TITANIUM_INGOT = register("titanium_ingot");
    public static final Item TITANIUM_NUGGET = register("titanium_nugget");
    public static final Item TITANIUM_PLATE = register("titanium_plate");

    // Zinc
    public static final Item SPHALERITE = register("sphalerite");
    public static final Item CRUSHED_SPHALERITE = register("crushed_sphalerite");
    public static final Item SPHALERITE_CONCENTRATE = register("sphalerite_concentrate");
    public static final Item ZINC_CALCINE = register("zinc_calcine");
    public static final Item ZINC_INGOT = register("zinc_ingot");
    public static final Item ZINC_NUGGET = register("zinc_nugget");

    // Cobalt
    public static final Item COBALTITE = register("cobaltite");
    public static final Item CRUSHED_COBALTITE = register("crushed_cobaltite");
    public static final Item COBALT_INGOT = register("cobalt_ingot");
    public static final Item COBALT_NUGGET = register("cobalt_nugget");

    // Lithium
    public static final Item CRUSHED_SPODUMENE = register("crushed_spodumene");
    public static final Item SPODUMENE_CONCENTRATE = register("spodumene_concentrate");
    public static final Item LITHIUM_CARBONATE = register("lithium_carbonate");
    public static final Item LITHIUM_INGOT = register("lithium_ingot");
    public static final Item LITHIUM_NUGGET = register("lithium_nugget");

    // Nickel
    public static final Item PENTLANDITE = register("pentlandite");
    public static final Item CRUSHED_PENTLANDITE = register("crushed_pentlandite");
    public static final Item PENTLANDITE_CONCENTRATE = register("pentlandite_concentrate");
    public static final Item NICKEL_INGOT = register("nickel_ingot");
    public static final Item NICKEL_NUGGET = register("nickel_nugget");

    // Iridium
    public static final Item IRIDIUM_INGOT = register("iridium_ingot");
    public static final Item IRIDIUM_NUGGET = register("iridium_nugget");

    // Silicon
    public static final Item CRUSHED_QUARTZ = register("crushed_quartz");
    public static final Item SILICON_ROD = register("silicon_rod");
    public static final Item SILICON_INGOT = register("silicon_ingot");
    public static final Item SILICON_PELLET = register("silicon_pellet");

    // Tin
    public static final Item CASSITERITE = register("cassiterite");
    public static final Item CRUSHED_CASSITERITE = register("crushed_cassiterite");
    public static final Item CASSITERITE_CONCENTRATE = register("cassiterite_concentrate");
    public static final Item TIN_INGOT = register("tin_ingot");
    public static final Item TIN_NUGGET = register("tin_nugget");

    // Rubber
    public static final Item COAGULATED_LATEX = register("coagulated_latex");
    public static final Item RAW_RUBBER = register("raw_rubber");
    public static final Item RUBBER = register("rubber");

    // Sulfur
    public static final Item PYRITE = register("pyrite");
    public static final Item CRUSHED_SULFUR = register("crushed_sulfur");
    public static final Item SULFUR = register("sulfur");

    // Steel
    public static final Item STEEL_INGOT = register("steel_ingot");
    public static final Item STEEL_NUGGET = register("steel_nugget");

    // Sodium
    public static final Item SODIUM_HYDROXIDE = register("sodium_hydroxide");
    public static final Item SODIUM_CARBONATE = register("sodium_carbonate");

    // Miscellaneous
    public static final Item RED_MUD = register("red_mud");
    public static final Item CRYOLITE = register("cryolite");
    public static final Item CARBON_ROD = registerWithSettings("carbon_rod", settings -> settings.maxDamage(50));
    public static final WrenchItem WRENCH = register("wrench", settings -> new WrenchItem(settings.maxCount(1)));

    public static Item register(String name) {
        return registerItem(name, new Item(new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, Industria.id(name)))));
    }

    public static Item registerWithSettings(String name, Function<Item.Settings, Item.Settings> settingsApplier) {
        return registerItem(name, new Item(settingsApplier.apply(new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, Industria.id(name))))));
    }

    public static <T extends Item> T register(String name, Function<Item.Settings, T> constructor, Function<Item.Settings, Item.Settings> settingsApplier) {
        return registerItem(name, constructor.apply(
                settingsApplier.apply(new Item.Settings().registryKey(
                        RegistryKey.of(RegistryKeys.ITEM, Industria.id(name))))));
    }

    public static <T extends Item> T register(String name, Function<Item.Settings, T> constructor) {
        return register(name, constructor, settings -> settings);
    }

    public static void init() {}

    private static <T extends Item> T registerItem(String name, T item) {
        return Registry.register(Registries.ITEM, Industria.id(name), item);
    }
}
