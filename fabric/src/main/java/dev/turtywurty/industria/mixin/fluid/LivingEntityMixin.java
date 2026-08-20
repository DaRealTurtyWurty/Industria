package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @ModifyExpressionValue(method = "baseTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$baseTick(boolean original) {
        if (original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canCauseDrowning)
                .anyMatch(data -> isEyeInFluid(data.fluidTag()));
    }

    @ModifyExpressionValue(method = "aiStep",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;isInWater()Z"))
    private boolean industria$isInWater(boolean original) {
        if (original)
            return true;

        for (FluidData fluidData : FluidData.FLUID_DATA.values()) {
            if (getFluidHeight(fluidData.fluidTag()) > 0.0D) {
                return true;
            }
        }

        return false;
    }

    @ModifyExpressionValue(method = "aiStep",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getFluidHeight(Lnet/minecraft/tags/TagKey;)D",
                    ordinal = 1))
    private double industria$getFluidHeight(double original) {
        if (original > 0.0D)
            return original;

        for (FluidData fluidData : FluidData.FLUID_DATA.values()) {
            double height = getFluidHeight(fluidData.fluidTag());
            if (height > 0.0D) {
                return height;
            }
        }

        return original;
    }
}
