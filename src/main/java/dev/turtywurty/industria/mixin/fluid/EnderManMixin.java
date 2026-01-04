package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnderMan.class)
public class EnderManMixin {
    @ModifyExpressionValue(method = "teleport(DDD)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$teleportTo(boolean original, @Local BlockState blockState) {
        if(original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canEndermanTeleportInto)
                .anyMatch(data -> blockState.getFluidState().is(data.fluidTag()));
    }
}
