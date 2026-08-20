package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// TODO: Blast resistance
@Mixin(RespawnAnchorBlock.class)
public class RespawnAnchorBlockMixin {
    @ModifyExpressionValue(method = "isWaterThatWouldFlow",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z",
                    ordinal = 0))
    private static boolean industria$hasStillWater$0(boolean original, BlockPos pos, Level world, @Local(name = "fluid") FluidState fluidState) {
        if (original)
            return true;

        FluidData fluidData = FluidData.FLUID_DATA.get(fluidState.getType());
        return fluidData == null || !fluidData.affectsRespawnAnchor();
    }

    @ModifyExpressionValue(method = "isWaterThatWouldFlow",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z",
                    ordinal = 1))
    private static boolean industria$hasStillWater$1(boolean original, BlockPos pos, Level world, @Local(name = "fluidBelow") FluidState fluidState) {
        if (original)
            return true;

        FluidData fluidData = FluidData.FLUID_DATA.get(fluidState.getType());
        return fluidData == null || !fluidData.affectsRespawnAnchor();
    }

    @ModifyExpressionValue(method = "explode",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$explode(boolean original, BlockState state, ServerLevel world, BlockPos explodedPos) {
        if (original)
            return true;

        FluidData fluidData = FluidData.FLUID_DATA.get(world.getFluidState(explodedPos.above()).getType());
        return fluidData != null && fluidData.affectsRespawnAnchor();
    }
}
