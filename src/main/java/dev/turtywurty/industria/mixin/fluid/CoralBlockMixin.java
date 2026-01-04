package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.world.level.block.CoralBlock;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CoralBlock.class)
public class CoralBlockMixin {
    @ModifyExpressionValue(method = "scanForWater",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$isInWater(boolean original, @Local FluidState fluidState) {
        if (original)
            return true;

        FluidData fluidData = FluidData.FLUID_DATA.get(fluidState.getType());
        return fluidData != null && fluidData.canCoralSurvive();
    }
}
