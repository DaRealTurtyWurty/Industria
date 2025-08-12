package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.block.SeagrassBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// TODO: Growing
@Mixin(SeagrassBlock.class)
public class SeagrassBlockMixin {
    @ModifyExpressionValue(method = "getPlacementState",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean industria$getPlacementState(boolean original, ItemPlacementContext ctx, @Local FluidState fluidState) {
        if (original)
            return true;

        FluidData data = FluidData.FLUID_DATA.get(fluidState.getFluid());
        return data != null && data.canSeagrassSurvive();
    }
}
