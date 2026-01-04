package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Drowned.class)
public class DrownedMixin {
    @ModifyExpressionValue(method = "checkDrownedSpawnRules(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)Z",
    at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z",
            ordinal = 0))
    private static boolean industria$canSpawn$0(boolean original, EntityType<?> type, ServerLevelAccessor world, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        if (original)
            return true;

        FluidState fluidState = world.getFluidState(pos.below());
        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canDrownedSpawn)
                .anyMatch(fluidData -> fluidState.is(fluidData.fluidTag()));
    }

    @ModifyExpressionValue(method = "checkDrownedSpawnRules(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/core/BlockPos;Lnet/minecraft/util/RandomSource;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z",
                    ordinal = 1))
    private static boolean industria$canSpawn$1(boolean original, EntityType<?> type, ServerLevelAccessor world, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        FluidState fluidState = world.getFluidState(pos);

        return original || FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canDrownedSpawn)
                .anyMatch(fluidData -> fluidState.is(fluidData.fluidTag()));
    }
}
