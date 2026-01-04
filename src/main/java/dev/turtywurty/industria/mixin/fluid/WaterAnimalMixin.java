package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WaterAnimal.class)
public class WaterAnimalMixin {
    @ModifyExpressionValue(method = "checkSurfaceWaterAnimalSpawnRules(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private static boolean industria$canSpawn(boolean original, EntityType<?> entityType, LevelAccessor world, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        if(original)
            return true;

        FluidState fluidState = world.getFluidState(pos);
        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canWaterCreatureSpawn)
                .anyMatch(data -> fluidState.is(data.fluidTag()));
    }
}
