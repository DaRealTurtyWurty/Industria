package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.block.BlockState;
import net.minecraft.entity.mob.EndermanEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EndermanEntity.class)
public class EndermanEntityMixin {
    @ModifyExpressionValue(method = "teleportTo(DDD)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean industria$teleportTo(boolean original, @Local BlockState blockState) {
        if (original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canEndermanTeleportInto)
                .anyMatch(data -> blockState.getFluidState().isIn(data.fluidTag()));
    }
}
