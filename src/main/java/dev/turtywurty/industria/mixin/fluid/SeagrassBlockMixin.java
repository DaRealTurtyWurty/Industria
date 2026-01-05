package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// TODO: Growing
@Mixin(SeagrassBlock.class)
public class SeagrassBlockMixin {
    @ModifyExpressionValue(method = "getStateForPlacement",
    at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$getPlacementState(boolean original, BlockPlaceContext ctx, @Local(name = "fluidState") FluidState fluidState) {
        if(original)
            return true;

        FluidData data = FluidData.FLUID_DATA.get(fluidState.getType());
        return data != null && data.canSeagrassSurvive();
    }
}
