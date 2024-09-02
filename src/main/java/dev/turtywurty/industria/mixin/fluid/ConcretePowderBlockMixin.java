package dev.turtywurty.industria.mixin.fluid;

import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConcretePowderBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ConcretePowderBlock.class)
public class ConcretePowderBlockMixin {
    @Inject(method="hardensIn", at=@At("HEAD"), cancellable=true)
    private static void industria$hardensIn(BlockState state, CallbackInfoReturnable<Boolean> callback) {
        FluidData fluidData = FluidData.FLUID_DATA.get(state.getFluidState().getFluid());
        if (fluidData != null && fluidData.hardensConcrete()) {
            callback.setReturnValue(true);
        }
    }
}
