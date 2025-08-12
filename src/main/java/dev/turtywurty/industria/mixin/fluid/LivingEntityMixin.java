package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalDoubleRef;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyExpressionValue(method = "baseTick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean industria$baseTick(boolean original) {
        if (original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canCauseDrowning)
                .anyMatch(data -> isSubmergedIn(data.fluidTag()));
    }

    @Inject(method = "tickMovement",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;isTouchingWater()Z"))
    private void industria$tickMovement(CallbackInfo callback, @Local(ordinal = 3) LocalDoubleRef g) {
        if (g.get() != 0.0D)
            return;

        for (FluidData fluidData : FluidData.FLUID_DATA.values()) {
            double height = getFluidHeight(fluidData.fluidTag());
            if (height > 0.0D) {
                g.set(height);
                break;
            }
        }
    }
}
