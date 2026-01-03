package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// TODO: Blast resistance
@Mixin(RespawnAnchorBlock.class)
public class RespawnAnchorBlockMixin {
    @ModifyExpressionValue(method = "hasStillWater",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z",
                    ordinal = 0))
    private static boolean industria$hasStillWater$0(boolean original, BlockPos pos, World world, @Local FluidState fluidState) {
        if (original)
            return true;

        FluidData fluidData = FluidData.FLUID_DATA.get(fluidState.getFluid());
        return fluidData == null || !fluidData.affectsRespawnAnchor();
    }

    @ModifyExpressionValue(method = "hasStillWater",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z",
                    ordinal = 1))
    private static boolean industria$hasStillWater$1(boolean original, BlockPos pos, World world, @Local(ordinal = 1) FluidState fluidState) {
        if (original)
            return true;

        FluidData fluidData = FluidData.FLUID_DATA.get(fluidState.getFluid());
        return fluidData == null || !fluidData.affectsRespawnAnchor();
    }

    @ModifyExpressionValue(method = "explode",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean industria$explode(boolean original, BlockState state, ServerWorld world, BlockPos explodedPos) {
        if (original)
            return true;

        FluidData fluidData = FluidData.FLUID_DATA.get(state.getFluidState().getFluid());
        return fluidData != null && fluidData.affectsRespawnAnchor();
    }
}
