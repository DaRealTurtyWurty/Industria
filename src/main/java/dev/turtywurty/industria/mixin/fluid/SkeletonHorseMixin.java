package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkeletonHorse.class)
public abstract class SkeletonHorseMixin extends AbstractHorse {
    protected SkeletonHorseMixin(EntityType<? extends AbstractHorse> entityType, Level world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(method = "getAmbientSound",
    at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/animal/equine/SkeletonHorse;isSubmergedIn(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$getAmbientSound(boolean original) {
        if(original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::useSkeletonHorseSubmergedSound)
                .anyMatch(fluidData -> isEyeInFluid(fluidData.fluidTag()));
    }
}
