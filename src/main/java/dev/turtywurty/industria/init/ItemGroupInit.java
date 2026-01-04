package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

public class ItemGroupInit {
    public static final Component MAIN_TITLE = Component.translatable("itemGroup." + Industria.MOD_ID + ".main");

    public static final CreativeModeTab MAIN_GROUP = register("main", FabricCreativeModeTab.builder()
            .title(MAIN_TITLE)
            .icon(() -> BlockInit.ALLOY_FURNACE.asItem().getDefaultInstance())
            .displayItems((displayContext, entries) ->
                    BuiltInRegistries.ITEM.registryKeySet().stream()
                            .filter(key -> key.identifier().getNamespace().equals(Industria.MOD_ID))
                            .map(BuiltInRegistries.ITEM::getValueOrThrow)
                            .forEach(entries::accept))
            .build());

    public static <T extends CreativeModeTab> T register(String name, T group) {
        return Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Industria.id(name), group);
    }

    public static void init() {
    }
}
