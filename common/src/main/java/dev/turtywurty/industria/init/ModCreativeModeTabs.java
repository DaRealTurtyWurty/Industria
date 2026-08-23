package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.turtymultiloader.platform.Platform;
import dev.turtywurty.turtymultiloader.registration.CreativeTabBuilder;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import java.util.Objects;
import java.util.function.Consumer;

public class ModCreativeModeTabs {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final Component MAIN_TITLE = Component.translatable("itemGroup." + Industria.MOD_ID + ".main");

    public static final RegistrationHandle<CreativeModeTab, CreativeModeTab> TAB = register("main", builder -> builder.title(MAIN_TITLE)
            .icon(() -> ModBlocks.ALLOY_FURNACE.get().asItem().getDefaultInstance())
            .displayItems((output) ->
                    BuiltInRegistries.ITEM.registryKeySet().stream()
                            .filter(key -> key.identifier().getNamespace().equals(Industria.MOD_ID))
                            .filter(key -> Platform.isDevelopmentEnvironment() || !Objects.equals(key, ModItems.MULTIBLOCK_EXPORTER.key()))
                            .map(BuiltInRegistries.ITEM::getValueOrThrow)
                            .forEach(output::accept)));

    public static RegistrationHandle<CreativeModeTab, CreativeModeTab> register(String name, Consumer<CreativeTabBuilder> group) {
        return REGISTRIES.registerCreativeTab(Industria.id(name), group);
    }

    public static void init() {
    }
}
