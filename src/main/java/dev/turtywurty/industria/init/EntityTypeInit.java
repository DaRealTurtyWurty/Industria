package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class EntityTypeInit {
    public static <T extends Entity> EntityType<T> register(String name, EntityType<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, Industria.id(name), type);
    }

    public static void init() {}
}
