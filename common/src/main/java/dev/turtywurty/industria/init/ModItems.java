package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.init.list.FoodList;
import dev.turtywurty.industria.item.*;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.function.Function;
import java.util.function.Supplier;

public class ModItems {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final RegistrationHandle<Item, SeismicScannerItem> SEISMIC_SCANNER = register("seismic_scanner",
            SeismicScannerItem::new, settings -> settings.stacksTo(1));

    public static final RegistrationHandle<Item, SimpleDrillHeadItem> SIMPLE_DRILL_HEAD = register("simple_drill_head",
            SimpleDrillHeadItem::new, settings -> settings.stacksTo(1));

    public static final RegistrationHandle<Item, BlockBuilderDrillHeadItem> BLOCK_BUILDER_DRILL_HEAD = register("block_builder_drill_head",
            BlockBuilderDrillHeadItem::new, settings -> settings.stacksTo(1));

    public static final RegistrationHandle<Item, RotaryKilnBlockItem> ROTARY_KILN = register("rotary_kiln", RotaryKilnBlockItem::new);

    // Aluminium
    public static final RegistrationHandle<Item, Item> BAUXITE = register("bauxite");
    public static final RegistrationHandle<Item, Item> CRUSHED_BAUXITE = register("crushed_bauxite");
    public static final RegistrationHandle<Item, Item> SODIUM_ALUMINATE = register("sodium_aluminate");
    public static final RegistrationHandle<Item, Item> ALUMINIUM_HYDROXIDE = register("aluminium_hydroxide");
    public static final RegistrationHandle<Item, Item> ALUMINA = register("alumina");
    public static final RegistrationHandle<Item, Item> ALUMINIUM_INGOT = register("aluminium_ingot");
    public static final RegistrationHandle<Item, Item> ALUMINIUM_NUGGET = register("aluminium_nugget");
    public static final RegistrationHandle<Item, Item> ALUMINIUM_PLATE = register("aluminium_plate");

    // Silver
    public static final RegistrationHandle<Item, Item> ARGENTITE = register("argentite");
    public static final RegistrationHandle<Item, Item> CRUSHED_ARGENTITE = register("crushed_argentite");
    public static final RegistrationHandle<Item, Item> ARGENTITE_CONCENTRATE = register("argentite_concentrate");
    public static final RegistrationHandle<Item, Item> LEAD_BULLION = register("lead_bullion");
    public static final RegistrationHandle<Item, Item> DORE_SILVER = register("dore_silver");
    public static final RegistrationHandle<Item, Item> SILVER_INGOT = register("silver_ingot");
    public static final RegistrationHandle<Item, Item> SILVER_NUGGET = register("silver_nugget");

    // Lead
    public static final RegistrationHandle<Item, Item> GALENA = register("galena");
    public static final RegistrationHandle<Item, Item> CRUSHED_GALENA = register("crushed_galena");
    public static final RegistrationHandle<Item, Item> GALENA_CONCENTRATE = register("galena_concentrate");
    public static final RegistrationHandle<Item, Item> TETRAGONAL_LITHARGE = register("tetragonal_litharge");
    public static final RegistrationHandle<Item, Item> LEAD_INGOT = register("lead_ingot");
    public static final RegistrationHandle<Item, Item> LEAD_NUGGET = register("lead_nugget");

    // Titanium
    public static final RegistrationHandle<Item, Item> ILMENITE = register("ilmenite");
    public static final RegistrationHandle<Item, Item> CRUSHED_ILMENITE = register("crushed_ilmenite");
    public static final RegistrationHandle<Item, Item> ILMENITE_CONCENTRATE = register("ilmenite_concentrate");
    public static final RegistrationHandle<Item, Item> TITANIUM_TETRACHLORIDE = register("titanium_tetrachloride");
    public static final RegistrationHandle<Item, Item> TITANIUM_INGOT = register("titanium_ingot");
    public static final RegistrationHandle<Item, Item> TITANIUM_NUGGET = register("titanium_nugget");
    public static final RegistrationHandle<Item, Item> TITANIUM_PLATE = register("titanium_plate");

    // Zinc
    public static final RegistrationHandle<Item, Item> SPHALERITE = register("sphalerite");
    public static final RegistrationHandle<Item, Item> CRUSHED_SPHALERITE = register("crushed_sphalerite");
    public static final RegistrationHandle<Item, Item> SPHALERITE_CONCENTRATE = register("sphalerite_concentrate");
    public static final RegistrationHandle<Item, Item> ZINC_CALCINE = register("zinc_calcine");
    public static final RegistrationHandle<Item, Item> ZINC_INGOT = register("zinc_ingot");
    public static final RegistrationHandle<Item, Item> ZINC_NUGGET = register("zinc_nugget");

    // Cobalt
    public static final RegistrationHandle<Item, Item> COBALTITE = register("cobaltite");
    public static final RegistrationHandle<Item, Item> CRUSHED_COBALTITE = register("crushed_cobaltite");
    public static final RegistrationHandle<Item, Item> COBALT_INGOT = register("cobalt_ingot");
    public static final RegistrationHandle<Item, Item> COBALT_NUGGET = register("cobalt_nugget");

    // Lithium
    public static final RegistrationHandle<Item, Item> CRUSHED_SPODUMENE = register("crushed_spodumene");
    public static final RegistrationHandle<Item, Item> SPODUMENE_CONCENTRATE = register("spodumene_concentrate");
    public static final RegistrationHandle<Item, Item> LITHIUM_CARBONATE = register("lithium_carbonate");
    public static final RegistrationHandle<Item, Item> LITHIUM_INGOT = register("lithium_ingot");
    public static final RegistrationHandle<Item, Item> LITHIUM_NUGGET = register("lithium_nugget");

    // Nickel
    public static final RegistrationHandle<Item, Item> PENTLANDITE = register("pentlandite");
    public static final RegistrationHandle<Item, Item> CRUSHED_PENTLANDITE = register("crushed_pentlandite");
    public static final RegistrationHandle<Item, Item> PENTLANDITE_CONCENTRATE = register("pentlandite_concentrate");
    public static final RegistrationHandle<Item, Item> NICKEL_INGOT = register("nickel_ingot");
    public static final RegistrationHandle<Item, Item> NICKEL_NUGGET = register("nickel_nugget");

    // Iridium
    public static final RegistrationHandle<Item, Item> IRIDIUM_INGOT = register("iridium_ingot");
    public static final RegistrationHandle<Item, Item> IRIDIUM_NUGGET = register("iridium_nugget");

    // Silicon
    public static final RegistrationHandle<Item, Item> CRUSHED_QUARTZ = register("crushed_quartz");
    public static final RegistrationHandle<Item, Item> SILICON_ROD = register("silicon_rod");
    public static final RegistrationHandle<Item, Item> SILICON_INGOT = register("silicon_ingot");
    public static final RegistrationHandle<Item, Item> SILICON_PELLET = register("silicon_pellet");

    // Tin
    public static final RegistrationHandle<Item, Item> CASSITERITE = register("cassiterite");
    public static final RegistrationHandle<Item, Item> CRUSHED_CASSITERITE = register("crushed_cassiterite");
    public static final RegistrationHandle<Item, Item> CASSITERITE_CONCENTRATE = register("cassiterite_concentrate");
    public static final RegistrationHandle<Item, Item> TIN_INGOT = register("tin_ingot");
    public static final RegistrationHandle<Item, Item> TIN_NUGGET = register("tin_nugget");

    // Rubber
    public static final RegistrationHandle<Item, Item> COAGULATED_LATEX = register("coagulated_latex");
    public static final RegistrationHandle<Item, Item> RAW_RUBBER = register("raw_rubber");
    public static final RegistrationHandle<Item, Item> RUBBER = register("rubber");

    // Sulfur
    public static final RegistrationHandle<Item, Item> PYRITE = register("pyrite");
    public static final RegistrationHandle<Item, Item> CRUSHED_SULFUR = register("crushed_sulfur");
    public static final RegistrationHandle<Item, Item> SULFUR = register("sulfur");

    // Steel
    public static final RegistrationHandle<Item, Item> STEEL_INGOT = register("steel_ingot");
    public static final RegistrationHandle<Item, Item> STEEL_NUGGET = register("steel_nugget");

    // Sodium
    public static final RegistrationHandle<Item, Item> SODIUM_HYDROXIDE = register("sodium_hydroxide");
    public static final RegistrationHandle<Item, Item> SODIUM_CARBONATE = register("sodium_carbonate");

    // Miscellaneous
    public static final RegistrationHandle<Item, Item> RED_MUD = register("red_mud");
    public static final RegistrationHandle<Item, Item> CRYOLITE = register("cryolite");
    public static final RegistrationHandle<Item, Item> CARBON_ROD = registerWithSettings("carbon_rod",
            settings -> settings.durability(50));
    public static final RegistrationHandle<Item, WrenchItem> WRENCH = register("wrench",
            settings -> new WrenchItem(settings.stacksTo(1)));
    public static final RegistrationHandle<Item, MultiblockExportItem> MULTIBLOCK_EXPORTER = register("multiblock_exporter",
            settings -> new MultiblockExportItem(settings.stacksTo(1)));
    public static final RegistrationHandle<Item, Item> EMPTY_MOB_JAR = register("mob_jar", Item::new);
    public static final RegistrationHandle<Item, MobJarItem> FILLED_MOB_JAR = register("filled_mob_jar",
            settings -> new MobJarItem(settings.craftRemainder(ModItems.EMPTY_MOB_JAR.get())));
    public static final RegistrationHandle<Item, Item> BOTTLE_FORMIC_ACID = registerWithSettings("bottle_formic_acid",
            settings -> settings.craftRemainder(Items.GLASS_BOTTLE)
                    .stacksTo(16)
                    .food(FoodList.FORMIC_ACID_FOOD, FoodList.FORMIC_ACID_CONSUMABLE));

    public static RegistrationHandle<Item, Item> register(String name) {
        return registerItem(name, () -> new Item(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, Industria.id(name)))));
    }

    public static RegistrationHandle<Item, Item> registerWithSettings(String name, Function<Item.Properties, Item.Properties> settingsApplier) {
        return registerItem(name, () -> new Item(settingsApplier.apply(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, Industria.id(name))))));
    }

    public static <T extends Item> RegistrationHandle<Item, T> register(String name, Function<Item.Properties, T> constructor, Function<Item.Properties, Item.Properties> settingsApplier) {
        return registerItem(name, () -> constructor.apply(
                settingsApplier.apply(new Item.Properties().setId(
                        ResourceKey.create(Registries.ITEM, Industria.id(name))))));
    }

    public static <T extends Item> RegistrationHandle<Item, T> register(String name, Function<Item.Properties, T> constructor) {
        return register(name, constructor, settings -> settings);
    }

    public static void init() {
    }

    private static <T extends Item> RegistrationHandle<Item, T> registerItem(String name, Supplier<T> item) {
        return REGISTRIES.registerItem(Industria.id(name), item);
    }
}
