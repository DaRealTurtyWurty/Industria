package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.item.BlockBuilderDrillHeadItem;
import dev.turtywurty.industria.item.RotaryKilnBlockItem;
import dev.turtywurty.industria.item.SeismicScannerItem;
import dev.turtywurty.industria.item.SimpleDrillHeadItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.function.Function;

public class ItemInit {
    public static final Item STEEL_INGOT = register("steel_ingot");

    public static final SeismicScannerItem SEISMIC_SCANNER = register("seismic_scanner",
            SeismicScannerItem::new, settings -> settings.maxCount(1));

    public static final SimpleDrillHeadItem SIMPLE_DRILL_HEAD = register("simple_drill_head",
            SimpleDrillHeadItem::new, settings -> settings.maxCount(1));

    public static final BlockBuilderDrillHeadItem BLOCK_BUILDER_DRILL_HEAD = register("block_builder_drill_head",
            BlockBuilderDrillHeadItem::new, settings -> settings.maxCount(1));

    public static final Item ALUMINIUM_INGOT = register("aluminium_ingot");
    public static final Item TIN_INGOT = register("tin_ingot");
    public static final Item ZINC_INGOT = register("zinc_ingot");

    public static final Item ALUMINIUM_NUGGET = register("aluminium_nugget");
    public static final Item TIN_NUGGET = register("tin_nugget");
    public static final Item ZINC_NUGGET = register("zinc_nugget");

    public static final Item RAW_BAUXITE = register("raw_bauxite");
    public static final Item RAW_CASSITERITE = register("raw_cassiterite");
    public static final Item RAW_ZINC = register("raw_zinc");

    public static final Item CRUSHED_CASSITERITE = register("crushed_cassiterite");
    public static final Item CASSITERITE_CONCENTRATE = register("cassiterite_concentrate"); // TODO: Add concentration level

    public static final Item SODIUM_HYDROXIDE = register("sodium_hydroxide");
    public static final Item SODIUM_ALUMINATE = register("sodium_aluminate");

    public static final Item RED_MUD = register("red_mud");
    public static final Item ALUMINIUM_HYDROXIDE = register("aluminium_hydroxide");
    public static final Item SODIUM_CARBONATE = register("sodium_carbonate");
    public static final Item ALUMINA = register("alumina");
    public static final Item CRYOLITE = register("cryolite");
    public static final Item CARBON_ROD = registerWithSettings("carbon_rod", settings -> settings.maxDamage(50));

    public static final RotaryKilnBlockItem ROTARY_KILN = register("rotary_kiln", RotaryKilnBlockItem::new);

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
