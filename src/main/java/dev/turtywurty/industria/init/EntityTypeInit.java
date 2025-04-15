package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class EntityTypeInit {
    public static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> type) {
        Identifier id = Industria.id(name);
        return Registry.register(Registries.ENTITY_TYPE, id, type.build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, id)));
    }

    public static void init() {}
}
