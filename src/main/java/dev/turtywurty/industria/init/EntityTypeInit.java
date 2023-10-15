package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.entity.LithiumItemEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityTypeInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Industria.MOD_ID);

    public static final RegistryObject<EntityType<LithiumItemEntity>> LITHIUM_ITEM_ENTITY = ENTITIES.register("lithium_item_entity",
            () -> EntityType.Builder.<LithiumItemEntity>of(LithiumItemEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(1)
                    .build(new ResourceLocation(Industria.MOD_ID, "lithium_item_entity").toString()));
}
