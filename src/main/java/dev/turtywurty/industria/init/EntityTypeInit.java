package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class EntityTypeInit {
    public static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> type) {
        Identifier id = Industria.id(name);
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, id, type.build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    }

    public static void init() {}
}
