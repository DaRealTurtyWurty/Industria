package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.MultiblockDefinition;
import dev.turtywurty.industria.multiblock.old.MultiblockType;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class IndustriaRegistries {
    public static final ResourceKey<Registry<MultiblockType<?>>> MULTIBLOCK_TYPES_KEY = ResourceKey.createRegistryKey(Industria.id("multiblock_types"));

    public static final Registry<MultiblockType<?>> MULTIBLOCK_TYPES =
            FabricRegistryBuilder.create(MULTIBLOCK_TYPES_KEY)
                    .attribute(RegistryAttribute.SYNCED)
                    .buildAndRegister();

    public static final ResourceKey<Registry<MultiblockDefinition>> MULTIBLOCK_DEFINITION_KEY =
            ResourceKey.createRegistryKey(Industria.id("multiblock_definition"));

    public static final Registry<MultiblockDefinition> MULTIBLOCK_DEFINITIONS =
            FabricRegistryBuilder.create(MULTIBLOCK_DEFINITION_KEY)
                    .attribute(RegistryAttribute.SYNCED)
                    .buildAndRegister();

    public static void init() {}
}
