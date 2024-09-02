package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.block.BlockState;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpreadableBlock.class)
public class SpreadableBlockMixin {
    @ModifyExpressionValue(method = "canSpread",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private static boolean industria$canSpread(boolean original, BlockState state, WorldView world, BlockPos pos, @Local(ordinal = 1) BlockPos upPos) {
        if (original)
            return true;

        FluidData data = FluidData.FLUID_DATA.get(world.getFluidState(upPos).getFluid());
        return data == null || !data.preventsBlockSpreading();
    }
}
