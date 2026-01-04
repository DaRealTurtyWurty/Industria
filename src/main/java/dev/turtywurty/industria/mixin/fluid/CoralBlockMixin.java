package dev.turtywurty.industria.mixin.fluid;

import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.CoralBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CoralBlock.class)
public class CoralBlockMixin {
    @Inject(method= "scanForWater", at=@At("HEAD"), cancellable=true)
    private void industria$isInWater(BlockGetter world, BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        FluidData fluidData = FluidData.FLUID_DATA.get(world.getFluidState(pos).getType());
        if (fluidData != null && fluidData.canCoralSurvive()) {
            callback.setReturnValue(true);
        }
    }
}
