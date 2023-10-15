package dev.turtywurty.industria.init;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.entity.LithiumItemEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class ItemInit {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Industria.MOD_ID);

    public static final RegistryObject<Item> LITHIUM = ITEMS.register("lithium",
            () -> new Item(new Item.Properties()) {
                @Override
                public boolean hasCustomEntity(ItemStack stack) {
                    return true;
                }

                @Override
                public @NotNull Entity createEntity(Level level, Entity location, ItemStack stack) {
                    LithiumItemEntity entity = EntityTypeInit.LITHIUM_ITEM_ENTITY.get().create(level);
                    if (entity == null)
                        throw new IllegalStateException("Failed to create entity.");

                    entity.setItem(stack);
                    entity.setPos(location.getX(), location.getY(), location.getZ());
                    entity.setDeltaMovement(location.getDeltaMovement());
                    entity.setDefaultPickUpDelay();
                    return entity;
                }
            });
}
