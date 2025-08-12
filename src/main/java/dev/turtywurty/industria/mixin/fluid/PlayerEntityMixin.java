package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean industria$updateTurtleHelmet(boolean original) {
        if (original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::shouldTurtleHelmetActivate)
                .anyMatch(data -> isSubmergedIn(data.fluidTag()));
    }

    @ModifyExpressionValue(method = "getBlockBreakingSpeed",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean industria$getBlockBreakingSpeed(boolean original) {
        if (original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::affectsBlockBreakSpeed)
                .anyMatch(data -> isSubmergedIn(data.fluidTag()));
    }
}
