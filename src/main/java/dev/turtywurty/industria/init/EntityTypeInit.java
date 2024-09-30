package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.entity.DrillHeadEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class EntityTypeInit {
    public static final EntityType<DrillHeadEntity> DRILL_HEAD = register("drill_head",
            EntityType.Builder.<DrillHeadEntity>create(DrillHeadEntity::new, SpawnGroup.MISC)
                    .build(Industria.id("drill_head").toString()));

    public static <T extends Entity> EntityType<T> register(String name, EntityType<T> type) {
        return Registry.register(Registries.ENTITY_TYPE, Industria.id(name), type);
    }

    public static void init() {}
}
