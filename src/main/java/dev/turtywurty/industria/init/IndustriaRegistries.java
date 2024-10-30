package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.MultiblockType;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class IndustriaRegistries {
    public static final RegistryKey<Registry<MultiblockType<?>>> MULTIBLOCK_TYPES_KEY = RegistryKey.ofRegistry(Industria.id("multiblock_types"));

    public static final Registry<MultiblockType<?>> MULTIBLOCK_TYPES =
            FabricRegistryBuilder.createSimple(MULTIBLOCK_TYPES_KEY)
                    .attribute(RegistryAttribute.SYNCED)
                    .buildAndRegister();

    public static void init() {}
}
