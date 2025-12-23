package dev.turtywurty.industria.mixin.fluid;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.turtywurty.industria.fluid.FluidData;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.ItemEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
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
    public abstract World getEntityWorld();

    @Shadow
    private BlockPos blockPos;

    @Shadow
    public abstract boolean updateMovementInFluid(TagKey<Fluid> tag, double speed);

    @Shadow
    protected boolean firstUpdate;

    @Shadow
    protected Object2DoubleMap<TagKey<Fluid>> fluidHeight;

    @Shadow
    protected boolean touchingWater;

    @Shadow
    public abstract void onLanding();

    @Shadow
    public abstract void extinguish();

    @Shadow
    private EntityDimensions dimensions;

    @Shadow
    protected abstract SoundEvent getSplashSound();

    @Shadow
    protected abstract SoundEvent getHighSpeedSplashSound();

    @ModifyExpressionValue(method = "updateSwimming",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean industria$updateSwimming(boolean original) {
        if (original)
            return true;

        FluidState state = getEntityWorld().getFluidState(this.blockPos);
        FluidData data = FluidData.FLUID_DATA.get(state.getFluid());
        return data != null && data.canSwim();
    }

    @ModifyExpressionValue(method = "updateWaterState",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;updateMovementInFluid(Lnet/minecraft/registry/tag/TagKey;D)Z"))
    private boolean industria$updateWaterState(boolean original, @Local double ultrawarmModifier) {
        if (original)
            return true;

        Entity entity = (Entity) (Object) this;

        for (FluidData fluidDatum : FluidData.FLUID_DATA.values()) {
            if (updateMovementInFluid(fluidDatum.fluidTag(), fluidDatum.fluidMovementSpeed().apply(entity, ultrawarmModifier))) {
                return true;
            }
        }

        return false;
    }

    @Inject(method = "applyGravity", at = @At("HEAD"), cancellable = true)
    private void industria$applyGravity(CallbackInfo callback) {
        if ((Entity) (Object) this instanceof ItemEntity itemEntity) {
            for (FluidData fluidData : FluidData.FLUID_DATA.values()) {
                if (!this.firstUpdate && this.fluidHeight.getDouble(fluidData.fluidTag()) > 0.0D) {
                    fluidData.applyBuoyancy().accept(itemEntity);
                    callback.cancel();
                }
            }
        }
    }

    @Inject(method = "checkWaterState",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;updateMovementInFluid(Lnet/minecraft/registry/tag/TagKey;D)Z"),
            cancellable = true)
    private void industria$checkWaterState(CallbackInfo callback) {
        Entity entity = (Entity) (Object) this;
        for (FluidData fluidData : FluidData.FLUID_DATA.values()) {
            if (!fluidData.canSwim())
                continue;

            if (updateMovementInFluid(fluidData.fluidTag(), fluidData.fluidMovementSpeed().apply(entity, 0.0D))) {
                if (!this.touchingWater && !this.firstUpdate) {
                    industria$onSwimmingStart(entity, dimensions, getSplashSound(), getHighSpeedSplashSound(), fluidData);
                }

                if (fluidData.shouldBreakLanding())
                    onLanding();
                this.touchingWater = true;
                if (fluidData.shouldExtinguish())
                    extinguish();

                callback.cancel();
            }
        }
    }

    @Unique
    private static void industria$onSwimmingStart(Entity thisEntity, EntityDimensions dimensions, SoundEvent entitySplashSound, SoundEvent entityHighSpeedSplashSound, FluidData fluidData) {
        Entity entity = Objects.requireNonNullElse(thisEntity.getControllingPassenger(), thisEntity);
        float distanceModifier = entity == thisEntity ? 0.2F : 0.9F;

        Vec3d velocity = entity.getVelocity();
        float volume = Math.min(1.0F, (float) Math.sqrt(velocity.x * velocity.x * 0.2F + velocity.y * velocity.y + velocity.z * velocity.z * 0.2F) * distanceModifier);
        if (volume < 0.25F) {
            thisEntity.playSound(entitySplashSound, volume,
                    1.0F + (thisEntity.getRandom().nextFloat() - thisEntity.getRandom().nextFloat()) * 0.4F);
        } else {
            thisEntity.playSound(entityHighSpeedSplashSound, volume,
                    1.0F + (thisEntity.getRandom().nextFloat() - thisEntity.getRandom().nextFloat()) * 0.4F);
        }

        float yPos = (float) MathHelper.floor(thisEntity.getY());
        for (int i = 0; (float) i < 1.0F + dimensions.width() * 20.0F; i++) {
            double xOffset = (thisEntity.getRandom().nextDouble() * 2.0 - 1.0) * (double) dimensions.width();
            double yOffset = (thisEntity.getRandom().nextDouble() * 2.0 - 1.0) * (double) dimensions.width();
            thisEntity.getEntityWorld().addParticleClient(fluidData.bubbleParticle(),
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
            thisEntity.getEntityWorld().addParticleClient(fluidData.splashParticle(),
                    thisEntity.getX() + xOffset,
                    yPos + 1.0F,
                    thisEntity.getZ() + yOffset,
                    velocity.x,
                    velocity.y,
                    velocity.z);
        }

        thisEntity.emitGameEvent(GameEvent.SPLASH);
    }
}
