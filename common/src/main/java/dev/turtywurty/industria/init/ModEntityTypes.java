package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.turtymultiloader.registration.EntityTypeBuilder;
import dev.turtywurty.turtymultiloader.registration.RegistrationHandle;
import dev.turtywurty.turtymultiloader.registration.RegistryService;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModEntityTypes {
    private static final RegistryService REGISTRIES = RegistryService.get();

    public static <T extends Entity> RegistrationHandle<EntityType<?>, EntityType<T>> register(String name, Supplier<EntityType.Builder<T>> type) {
        Identifier id = Industria.id(name);
        return REGISTRIES.registerEntityType(id, () -> type.get().build(ResourceKey.create(Registries.ENTITY_TYPE, id)));
    }

    public static <T extends LivingEntity> RegistrationHandle<EntityType<?>, EntityType<T>> register(String name, EntityType.EntityFactory<T> factory, MobCategory category, Consumer<EntityTypeBuilder<T>> configuration) {
        return REGISTRIES.registerEntityType(Industria.id(name), factory, category, configuration);
    }

    public static void init() {
    }
}
