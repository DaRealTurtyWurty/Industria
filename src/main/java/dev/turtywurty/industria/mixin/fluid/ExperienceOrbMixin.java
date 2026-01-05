package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin extends Entity {
    public ExperienceOrbMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @ModifyExpressionValue(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ExperienceOrb;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$tick(boolean original) {
        if (original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::applyWaterMovement)
                .anyMatch(fluidData -> isEyeInFluid(fluidData.fluidTag()));
    }
}
