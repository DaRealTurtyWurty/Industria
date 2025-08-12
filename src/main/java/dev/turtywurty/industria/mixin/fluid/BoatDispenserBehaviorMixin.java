package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.block.dispenser.BoatDispenserBehavior;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BoatDispenserBehavior.class)
public class BoatDispenserBehaviorMixin {
    @ModifyExpressionValue(method = "dispenseSilently",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean industria$dispenseSilently(boolean original, BlockPointer pointer, ItemStack stack, @Local ServerWorld serverWorld, @Local BlockPos blockPos) {
        if (original)
            return true;

        FluidData data = FluidData.FLUID_DATA.get(serverWorld.getFluidState(blockPos).getFluid());
        return data != null && data.shouldDispenseBoatsAbove();
    }
}
