package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Guardian.class)
public class GuardianMixin {
    @ModifyExpressionValue(method = "getWalkTargetValue",
    at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$getPathfindingFavor(boolean original, BlockPos pos, LevelReader world) {
        if(original)
            return true;

        FluidState fluidState = world.getFluidState(pos);
        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canGuardianSpawn)
                .anyMatch(data -> fluidState.is(data.fluidTag()));
    }
}
