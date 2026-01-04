package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractBoat.class)
public abstract class AbstractBoatMixin extends VehicleEntity {
    public AbstractBoatMixin(EntityType<?> entityType, Level world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(method = "getWaterLevelAbove",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$getWaterHeightBelow(boolean original, @Local FluidState fluidState) {
        if (original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canBoatsWork)
                .anyMatch(data -> fluidState.is(data.fluidTag()));
    }

    @ModifyExpressionValue(method = "checkInWater",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$checkBoatInWater(boolean original, @Local FluidState fluidState) {
        if (original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canBoatsWork)
                .anyMatch(data -> fluidState.is(data.fluidTag()));
    }

    @ModifyExpressionValue(method = "isUnderwater",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$getUnderWaterLocation(boolean original, @Local FluidState fluidState) {
        if (original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canBoatsWork)
                .anyMatch(data -> fluidState.is(data.fluidTag()));
    }

    @ModifyExpressionValue(method = "checkFallDamage",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$fall(boolean original, double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        if (original)
            return true;

        FluidState fluidState = level().getFluidState(blockPosition().below());
        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canBoatsWork)
                .noneMatch(data -> fluidState.is(data.fluidTag()));
    }

    @ModifyExpressionValue(method = "canAddPassenger",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$canAddPassenger(boolean original) {
        if (original)
            return true;

        return FluidData.FLUID_DATA.values().stream()
                .filter(FluidData::canBoatsWork)
                .anyMatch(data -> isEyeInFluid(data.fluidTag()));
    }
}
