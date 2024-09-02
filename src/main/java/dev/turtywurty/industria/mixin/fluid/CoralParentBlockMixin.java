package dev.turtywurty.industria.mixin.fluid;

import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.block.BlockState;
import net.minecraft.block.CoralParentBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CoralParentBlock.class)
public class CoralParentBlockMixin {
    @Inject(method="isInWater", at=@At("HEAD"), cancellable=true)
    private static void industria$isInWater(BlockState state, BlockView world, BlockPos pos, CallbackInfoReturnable<Boolean> callback) {
        FluidData fluidData = FluidData.FLUID_DATA.get(world.getFluidState(pos).getFluid());
        if (fluidData != null && fluidData.canCoralSurvive()) {
            callback.setReturnValue(true);
        }
    }
}
