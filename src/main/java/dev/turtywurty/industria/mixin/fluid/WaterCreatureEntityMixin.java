package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WaterCreatureEntity.class)
public class WaterCreatureEntityMixin {
    @ModifyExpressionValue(method = "canSpawn(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/entity/SpawnReason;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)Z",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private static boolean industria$canSpawn(boolean original, EntityType<?> entityType, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        if(original)
            return true;

        FluidState fluidState = world.getFluidState(pos);
        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canWaterCreatureSpawn)
                .anyMatch(data -> fluidState.isIn(data.fluidTag()));
    }
}
