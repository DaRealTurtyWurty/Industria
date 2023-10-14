package dev.turtywurty.industria.mixin;

import dev.turtywurty.industria.init.ItemInit;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {
    @ModifyVariable(
            method = "addEntity",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/minecraft/world/entity/EntityType;updateInterval()I"
            ),
            index = 4,
            name = "j"
    )
    private int industria$modifyUpdateInterval(int original, Entity entity) {
        if (entity instanceof ItemEntity itemEntity) {
            Item item = itemEntity.getItem().getItem();
            if (item == ItemInit.LITHIUM.get()) {
                return 1;
            }
        }

        return original;
    }
}
