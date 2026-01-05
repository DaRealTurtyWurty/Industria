package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract Level level();

    @Shadow
    private BlockPos blockPosition;

    @Shadow
    public abstract boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> tag, double speed);

    @Shadow
    protected boolean firstTick;

    @Final
    @Shadow
    protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

    @Shadow
    protected boolean wasTouchingWater;

    @Shadow
    public abstract void resetFallDistance();

    @Shadow
    public abstract void clearFire();

    @Shadow
    private EntityDimensions dimensions;

    @Shadow
    protected abstract SoundEvent getSwimSplashSound();

    @Shadow
    protected abstract SoundEvent getSwimHighSpeedSplashSound();

    @ModifyExpressionValue(method = "updateSwimming",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean industria$updateSwimming(boolean original) {
        if (original)
            return true;

        FluidState state = level().getFluidState(this.blockPosition);
        FluidData data = FluidData.FLUID_DATA.get(state.getType());
        return data != null && data.canSwim();
    }

    @ModifyExpressionValue(method = "updateInWaterStateAndDoFluidPushing",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;updateFluidHeightAndDoFluidPushing(Lnet/minecraft/tags/TagKey;D)Z"))
    private boolean industria$updateWaterState(boolean original, @Local(name = "lavaFlowScale") double lavaFlowScale) {
        if (original)
            return true;

        Entity entity = (Entity) (Object) this;

        for (FluidData fluidDatum : FluidData.FLUID_DATA.values()) {
            if (updateFluidHeightAndDoFluidPushing(fluidDatum.fluidTag(), fluidDatum.fluidMovementSpeed().apply(entity, lavaFlowScale))) {
                return true;
            }
        }

        return false;
    }

    @Inject(method = "applyGravity", at = @At("HEAD"), cancellable = true)
    private void industria$applyGravity(CallbackInfo callback) {
        if ((Entity) (Object) this instanceof ItemEntity itemEntity) {
            for (FluidData fluidData : FluidData.FLUID_DATA.values()) {
                if (!this.firstTick && this.fluidHeight.getDouble(fluidData.fluidTag()) > 0.0D) {
                    fluidData.applyBuoyancy().accept(itemEntity);
                    callback.cancel();
                }
            }
        }
    }

    @Inject(method = "updateInWaterStateAndDoWaterCurrentPushing",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;updateFluidHeightAndDoFluidPushing(Lnet/minecraft/tags/TagKey;D)Z"),
            cancellable = true)
    private void industria$checkWaterState(CallbackInfo callback) {
        Entity entity = (Entity) (Object) this;
        for (FluidData fluidData : FluidData.FLUID_DATA.values()) {
            if (!fluidData.canSwim())
                continue;

            if (updateFluidHeightAndDoFluidPushing(fluidData.fluidTag(), fluidData.fluidMovementSpeed().apply(entity, 0.0D))) {
                if (!this.wasTouchingWater && !this.firstTick) {
                    industria$onSwimmingStart(entity, dimensions, getSwimSplashSound(), getSwimHighSpeedSplashSound(), fluidData);
                }

                if (fluidData.shouldBreakLanding())
                    resetFallDistance();
                this.wasTouchingWater = true;
                if (fluidData.shouldExtinguish())
                    clearFire();

                callback.cancel();
            }
        }
    }

    @Unique
    private static void industria$onSwimmingStart(Entity thisEntity, EntityDimensions dimensions, SoundEvent entitySplashSound, SoundEvent entityHighSpeedSplashSound, FluidData fluidData) {
        Entity entity = Objects.requireNonNullElse(thisEntity.getControllingPassenger(), thisEntity);
        float distanceModifier = entity == thisEntity ? 0.2F : 0.9F;

        Vec3 velocity = entity.getDeltaMovement();
        float volume = Math.min(1.0F, (float) Math.sqrt(velocity.x * velocity.x * 0.2F + velocity.y * velocity.y + velocity.z * velocity.z * 0.2F) * distanceModifier);
        if (volume < 0.25F) {
            thisEntity.playSound(entitySplashSound, volume,
                    1.0F + (thisEntity.getRandom().nextFloat() - thisEntity.getRandom().nextFloat()) * 0.4F);
        } else {
            thisEntity.playSound(entityHighSpeedSplashSound, volume,
                    1.0F + (thisEntity.getRandom().nextFloat() - thisEntity.getRandom().nextFloat()) * 0.4F);
        }

        float yPos = (float) Mth.floor(thisEntity.getY());
        for (int i = 0; (float) i < 1.0F + dimensions.width() * 20.0F; i++) {
            double xOffset = (thisEntity.getRandom().nextDouble() * 2.0 - 1.0) * (double) dimensions.width();
            double yOffset = (thisEntity.getRandom().nextDouble() * 2.0 - 1.0) * (double) dimensions.width();
            thisEntity.level().addParticle(fluidData.bubbleParticle(),
                    thisEntity.getX() + xOffset,
                    yPos + 1.0F,
                    thisEntity.getZ() + yOffset,
                    velocity.x,
                    velocity.y - thisEntity.getRandom().nextDouble() * 0.2F,
                    velocity.z);
        }

        for (int i = 0; (float) i < 1.0F + dimensions.width() * 20.0F; i++) {
            double xOffset = (thisEntity.getRandom().nextDouble() * 2.0 - 1.0) * (double) dimensions.width();
            double yOffset = (thisEntity.getRandom().nextDouble() * 2.0 - 1.0) * (double) dimensions.width();
            thisEntity.level().addParticle(fluidData.splashParticle(),
                    thisEntity.getX() + xOffset,
                    yPos + 1.0F,
                    thisEntity.getZ() + yOffset,
                    velocity.x,
                    velocity.y,
                    velocity.z);
        }

        thisEntity.gameEvent(GameEvent.SPLASH);
    }
}
