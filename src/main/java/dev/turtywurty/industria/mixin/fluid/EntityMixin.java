package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.turtywurty.industria.fluid.FluidData;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Unique
    private boolean industria$hasExtendedFluidTracking;

    @Shadow
    public abstract Level level();

    @Shadow
    private BlockPos blockPosition;

    @Shadow
    protected boolean firstTick;

    @Final
    @Mutable
    @Shadow
    private EntityFluidInteraction fluidInteraction;

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

    @Shadow
    public abstract boolean isPushedByFluid();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void industria$extendTrackedFluids(EntityType<?> type, Level level, CallbackInfo callback) {
        this.fluidInteraction = new EntityFluidInteraction(industria$getTrackedFluidTags());
        this.industria$hasExtendedFluidTracking = !FluidData.FLUID_DATA.isEmpty();
    }

    @Inject(method = "updateFluidInteraction", at = @At("HEAD"))
    private void industria$ensureExtendedFluidTracking(CallbackInfoReturnable<Boolean> callback) {
        if (!this.industria$hasExtendedFluidTracking && !FluidData.FLUID_DATA.isEmpty()) {
            this.fluidInteraction = new EntityFluidInteraction(industria$getTrackedFluidTags());
            this.industria$hasExtendedFluidTracking = true;
        }
    }

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

    @Inject(method = "updateFluidInteraction", at = @At("TAIL"), cancellable = true)
    private void industria$updateFluidInteraction(CallbackInfoReturnable<Boolean> callback) {
        Entity entity = (Entity) (Object) this;
        boolean wasInCustomFluid = false;
        double flowScale = this.level().environmentAttributes().getDimensionValue(EnvironmentAttributes.FAST_LAVA)
                ? 0.007
                : 0.0023333333333333335;

        for (FluidData fluidData : FluidData.FLUID_DATA.values()) {
            if (entity.getFluidHeight(fluidData.fluidTag()) <= 0.0D)
                continue;

            wasInCustomFluid = true;

            if (this.isPushedByFluid()) {
                this.fluidInteraction.applyCurrentTo(fluidData.fluidTag(), entity,
                        fluidData.fluidMovementSpeed().apply(entity, flowScale));
            }

            if (!fluidData.canSwim())
                continue;

            if (!this.wasTouchingWater && !this.firstTick) {
                industria$onSwimmingStart(entity, this.dimensions, this.getSwimSplashSound(), this.getSwimHighSpeedSplashSound(), fluidData);
            }

            if (fluidData.shouldBreakLanding())
                this.resetFallDistance();
            this.wasTouchingWater = true;
            if (fluidData.shouldExtinguish())
                this.clearFire();
        }

        if (wasInCustomFluid) {
            callback.setReturnValue(true);
        }
    }

    @Inject(method = "applyGravity", at = @At("HEAD"), cancellable = true)
    private void industria$applyGravity(CallbackInfo callback) {
        if ((Entity) (Object) this instanceof ItemEntity itemEntity) {
            Entity entity = (Entity) (Object) this;
            for (FluidData fluidData : FluidData.FLUID_DATA.values()) {
                if (!this.firstTick && entity.getFluidHeight(fluidData.fluidTag()) > 0.0D && fluidData.applyBuoyancy() != null) {
                    fluidData.applyBuoyancy().accept(itemEntity);
                    callback.cancel();
                    return;
                }
            }
        }
    }

    @Unique
    private static Set<TagKey<Fluid>> industria$getTrackedFluidTags() {
        Set<TagKey<Fluid>> trackedTags = new HashSet<>(Set.of(FluidTags.WATER, FluidTags.LAVA));
        for (FluidData fluidData : FluidData.FLUID_DATA.values()) {
            trackedTags.add(fluidData.fluidTag());
        }

        return trackedTags;
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
