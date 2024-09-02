package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TropicalFishEntity.class)
public class TropicalFishEntityMixin {
    @ModifyExpressionValue(method = "canTropicalFishSpawn",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private static boolean industria$canTropicalFishSpawn$0(boolean original, EntityType<TropicalFishEntity> type, WorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        if(original)
            return true;

        FluidState fluidState = world.getFluidState(pos.down());
        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canTropicalFishSpawn)
                .anyMatch(data -> fluidState.isIn(data.fluidTag()));
    }

    @ModifyExpressionValue(method = "canTropicalFishSpawn",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
    private static boolean industria$canTropicalFishSpawn$1(boolean original, EntityType<TropicalFishEntity> type, WorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        if(original)
            return true;

        FluidState fluidState = world.getFluidState(pos.up());
        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canTropicalFishSpawn)
                .anyMatch(data -> fluidState.isIn(data.fluidTag()));
    }
}
