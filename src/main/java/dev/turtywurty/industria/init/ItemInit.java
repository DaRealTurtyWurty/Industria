package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ItemInit {
    public static final Item STEEL_INGOT = register("steel_ingot");

    public static <T extends Item> T register(String name, T item) {
        return Registry.register(Registries.ITEM, Industria.id(name), item);
    }

    public static Item register(String name) {
        return Registry.register(Registries.ITEM, Industria.id(name), new Item(new Item.Settings()));
    }

    public static void init() {}
}
