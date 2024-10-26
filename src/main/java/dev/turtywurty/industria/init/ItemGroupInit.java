package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;

public class ItemGroupInit {
    public static final Text MAIN_TITLE = Text.translatable("itemGroup." + Industria.MOD_ID + ".main");

    public static final ItemGroup MAIN_GROUP = register("main", FabricItemGroup.builder()
            .displayName(MAIN_TITLE)
            .icon(() -> BlockInit.ALLOY_FURNACE.asItem().getDefaultStack())
            .entries((displayContext, entries) ->
                    Registries.ITEM.getKeys().stream()
                            .filter(key -> key.getValue().getNamespace().equals(Industria.MOD_ID))
                            .map(Registries.ITEM::getValueOrThrow)
                            .forEach(entries::add))
            .build());

    public static <T extends ItemGroup> T register(String name, T group) {
        return Registry.register(Registries.ITEM_GROUP, Industria.id(name), group);
    }

    public static void init() {
    }
}
