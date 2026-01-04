package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockEntityMixin extends Entity {
    public FallingBlockEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @ModifyExpressionValue(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z",
                    ordinal = 0))
    private boolean industria$tick$0(boolean original, @Local BlockPos blockPos) {
        if(original)
            return true;

        FluidState fluidState = level().getFluidState(blockPos);
        FluidData fluidData = FluidData.FLUID_DATA.get(fluidState.getType());
        return fluidData != null && fluidData.applyWaterMovement();
    }

    @ModifyExpressionValue(method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z",
                    ordinal = 1))
    private boolean industria$tick$1(boolean original, @Local BlockHitResult clip) {
        if(original)
            return true;

        FluidState fluidState = level().getFluidState(clip.getBlockPos());
        FluidData fluidData = FluidData.FLUID_DATA.get(fluidState.getType());
        return fluidData != null && fluidData.applyWaterMovement();
    }
}
