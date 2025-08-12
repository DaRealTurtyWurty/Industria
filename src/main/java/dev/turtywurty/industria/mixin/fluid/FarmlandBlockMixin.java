package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin {
    @ModifyExpressionValue(method = "isWaterNearby",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private static boolean industria$isWaterNearby(boolean original, WorldView world, BlockPos pos) {
        if (original)
            return true;

        FluidData fluidData = FluidData.FLUID_DATA.get(world.getFluidState(pos).getFluid());
        return fluidData != null && fluidData.canMoisturizeFarmland();
    }
}
