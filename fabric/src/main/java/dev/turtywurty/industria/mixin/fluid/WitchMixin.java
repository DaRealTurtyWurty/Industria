package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Witch.class)
public abstract class WitchMixin extends Raider {
    protected WitchMixin(EntityType<? extends Raider> entityType, Level world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(method = "aiStep",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Witch;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$tickMovement(boolean original) {
        if(original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::shouldWitchDrinkWaterBreathing)
                .anyMatch(fluidData -> isEyeInFluid(fluidData.fluidTag()));
    }
}
