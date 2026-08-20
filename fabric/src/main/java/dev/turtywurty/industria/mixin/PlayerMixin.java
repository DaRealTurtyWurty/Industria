package dev.turtywurty.industria.mixin;

import dev.turtywurty.industria.util.IHasPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin {
    @Shadow
    protected FoodData foodData;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(CallbackInfo callback) {
        ((IHasPlayer) this.foodData).industria$setPlayer((Player) (Object) this);
    }
}
