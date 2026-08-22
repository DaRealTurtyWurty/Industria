package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.old.MultiblockType;
import dev.turtywurty.turtymultiloader.registration.CustomRegistry;
import dev.turtywurty.turtymultiloader.registration.CustomRegistryOptions;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class IndustriaRegistries {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static final ResourceKey<Registry<MultiblockType<?>>> MULTIBLOCK_TYPES_KEY = ResourceKey.createRegistryKey(Industria.id("multiblock_types"));
    public static final CustomRegistry<MultiblockType<?>> MULTIBLOCK_TYPES =
            REGISTRIES.customRegistry(MULTIBLOCK_TYPES_KEY, new CustomRegistryOptions(true, false));

    public static void init() {
    }
}
