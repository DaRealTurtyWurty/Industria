package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TropicalFish.class)
public class TropicalFishMixin {
    @ModifyExpressionValue(method = "checkTropicalFishSpawnRules",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private static boolean industria$canTropicalFishSpawn$0(boolean original, EntityType<TropicalFish> type, LevelAccessor world, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if(original)
            return true;

        FluidState fluidState = world.getFluidState(pos.below());
        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canTropicalFishSpawn)
                .anyMatch(data -> fluidState.is(data.fluidTag()));
    }

    @ModifyExpressionValue(method = "checkTropicalFishSpawnRules",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private static boolean industria$canTropicalFishSpawn$1(boolean original, EntityType<TropicalFish> type, LevelAccessor world, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if(original)
            return true;

        FluidState fluidState = world.getFluidState(pos.above());
        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canTropicalFishSpawn)
                .anyMatch(data -> fluidState.is(data.fluidTag()));
    }
}
