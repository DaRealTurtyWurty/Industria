package dev.turtywurty.industria.mixin.fluid;

import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.block.CoralBlockBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CoralBlockBlock.class)
public class CoralBlockBlockMixin {
    @Inject(method="isInWater", at=@At("HEAD"), cancellable=true)
    private void industria$isInWater(BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        FluidData fluidData = FluidData.FLUID_DATA.get(world.getFluidState(pos).getFluid());
        if (fluidData != null && fluidData.canCoralSurvive()) {
            callback.setReturnValue(true);
        }
    }
}
