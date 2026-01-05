package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.FarmBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FarmBlock.class)
public class FarmBlockMixin {
    @ModifyExpressionValue(method = "isNearWater",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private static boolean industria$isWaterNearby(boolean original, LevelReader world, BlockPos pos, @Local(name = "blockPos") BlockPos blockPos) {
        if(original)
            return true;

        FluidData fluidData = FluidData.FLUID_DATA.get(world.getFluidState(blockPos).getType());
        return fluidData != null && fluidData.canMoisturizeFarmland();
    }
}
