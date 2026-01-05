package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpreadingSnowyDirtBlock.class)
public class SpreadingSnowyDirtBlockMixin {
    @ModifyExpressionValue(method = "canPropagate",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private static boolean industria$canSpread(boolean original, BlockState state, LevelReader world, BlockPos pos, @Local(name = "above") BlockPos upPos) {
        if (original)
            return true;

        FluidData data = FluidData.FLUID_DATA.get(world.getFluidState(upPos).getType());
        return data == null || !data.preventsBlockSpreading();
    }
}
