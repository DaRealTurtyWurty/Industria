package dev.turtywurty.industria.mixin;

import dev.turtywurty.industria.event.ItemEnterWaterEvent;
import dev.turtywurty.industria.init.ItemInit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;setUnderwaterMovement()V"),
            cancellable = true
    )
    private void industria$tick(CallbackInfo callback) {
        ItemEntity entity = industria$self();
        if(MinecraftForge.EVENT_BUS.post(new ItemEnterWaterEvent(entity))) {
            callback.cancel();
        }
    }

    @Unique
    private ItemEntity industria$self() {
        return (ItemEntity) (Object) this;
    }
}
