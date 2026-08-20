package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @ModifyExpressionValue(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$updateTurtleHelmet(boolean original) {
        if(original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::shouldTurtleHelmetActivate)
                .anyMatch(data -> isEyeInFluid(data.fluidTag()));
    }

    @ModifyExpressionValue(method = "getDestroySpeed",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$getBlockBreakingSpeed(boolean original) {
        if(original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::affectsBlockBreakSpeed)
                .anyMatch(data -> isEyeInFluid(data.fluidTag()));
    }
}
