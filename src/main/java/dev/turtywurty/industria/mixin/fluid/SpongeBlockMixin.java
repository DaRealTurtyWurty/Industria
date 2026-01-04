package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SpongeBlock;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SpongeBlock.class)
public class SpongeBlockMixin {
    @ModifyExpressionValue(method = "tryAbsorbWater",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private static boolean industria$absorbWater(boolean original, BlockPos pos, Level world, BlockPos currentPos, @Local FluidState fluidState) {
        if (original)
            return true;

        FluidData data = FluidData.FLUID_DATA.get(fluidState.getType());
        return data == null || !data.canSpongeAbsorb();
    }
}
