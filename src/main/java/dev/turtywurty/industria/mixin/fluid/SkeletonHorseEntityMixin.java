package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SkeletonHorseEntity.class)
public abstract class SkeletonHorseEntityMixin extends AbstractHorseEntity {
    protected SkeletonHorseEntityMixin(EntityType<? extends AbstractHorseEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(method = "getAmbientSound",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/SkeletonHorseEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean industria$getAmbientSound(boolean original) {
        if (original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::useSkeletonHorseSubmergedSound)
                .anyMatch(fluidData -> isSubmergedIn(fluidData.fluidTag()));
    }
}
