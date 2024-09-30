package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.item.DrillHeadItem;
import dev.turtywurty.industria.item.SeismicScannerItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ItemInit {
    public static final Item STEEL_INGOT = register("steel_ingot");

    public static final SeismicScannerItem SEISMIC_SCANNER = register("seismic_scanner",
            new SeismicScannerItem(new Item.Settings().maxCount(1)));

    public static final DrillHeadItem SIMPLE_DRILL_HEAD = register("simple_drill_head",
            new DrillHeadItem(new Item.Settings().maxCount(1)));

    public static <T extends Item> T register(String name, T item) {
        return Registry.register(Registries.ITEM, Industria.id(name), item);
    }

    public static Item register(String name) {
        return Registry.register(Registries.ITEM, Industria.id(name), new Item(new Item.Settings()));
    }

    public static void init() {}
}
