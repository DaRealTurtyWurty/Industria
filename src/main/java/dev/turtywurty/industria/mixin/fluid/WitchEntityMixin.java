package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WitchEntity.class)
public abstract class WitchEntityMixin extends RaiderEntity {
    protected WitchEntityMixin(EntityType<? extends RaiderEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(method = "tickMovement",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/WitchEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean industria$tickMovement(boolean original) {
        if (original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::shouldWitchDrinkWaterBreathing)
                .anyMatch(fluidData -> isSubmergedIn(fluidData.fluidTag()));
    }
}
