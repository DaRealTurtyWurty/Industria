package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SugarCaneBlock.class)
public class SugarCaneBlockMixin {
    @ModifyExpressionValue(method = "canSurvive",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$canPlaceAt(boolean original, BlockState state, LevelReader world, BlockPos pos, @Local(name = "fluidState") FluidState fluidState) {
        if (original)
            return true;

        FluidData data = FluidData.FLUID_DATA.get(fluidState.getType());
        return data != null && data.canSugarCaneUse();
    }
}
