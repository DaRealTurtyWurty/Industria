package dev.turtywurty.industria.fluid;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public record FluidData(@NotNull TagKey<Fluid> fluidTag, boolean hardensConcrete, boolean canCoralSurvive,
                        boolean canMoisturizeFarmland, boolean canKelpSurvive, boolean affectsRespawnAnchor,
                        boolean canSeagrassSurvive, boolean canSpongeAbsorb, boolean preventsBlockSpreading,
                        boolean canSugarCaneUse, boolean shouldDispenseBoatsAbove, boolean canSwim,
                        @NotNull BiFunction<Entity, Double, Float> fluidMovementSpeed, boolean applyWaterMovement,
                        @NotNull Consumer<ItemEntity> applyBuoyancy, boolean canCauseDrowning, boolean canDrownedSpawn,
                        boolean canEndermanTeleportInto, boolean canGuardianSpawn,
                        boolean useSkeletonHorseSubmergedSound, boolean canWaterCreatureSpawn,
                        boolean shouldWitchDrinkWaterBreathing, boolean willZombiesConvert,
                        boolean canTropicalFishSpawn, boolean shouldTurtlesFavor, boolean shouldTurtleHelmetActivate,
                        boolean affectsBlockBreakSpeed, boolean canBoatsWork, boolean shouldEvaporateInUltrawarm,
                        @NotNull ParticleOptions bubbleParticle, @NotNull ParticleOptions splashParticle,
                        boolean shouldBreakLanding, boolean shouldExtinguish) {
    public static final Map<Fluid, FluidData> FLUID_DATA = new HashMap<>();

    public static void registerFluidData(@NotNull Fluid fluid, @NotNull FluidData.Builder fluidData) {
        registerFluidData(fluid, fluidData.build());
    }

    public static void registerFluidData(@NotNull Fluid fluid, @NotNull FluidData fluidData) {
        FLUID_DATA.put(fluid, fluidData);
    }

    public static class Builder {
        private final TagKey<Fluid> fluidTag;
        private boolean hardensConcrete = false;
        private boolean canCoralSurvive = false;
        private boolean canMoisturizeFarmland = false;
        private boolean canKelpSurvive = false;
        private boolean affectsRespawnAnchor = false;
        private boolean canSeagrassSurvive = false;
        private boolean canSpongeAbsorb = false;
        private boolean preventsBlockSpreading = false;
        private boolean canSugarCaneUse = false;
        private boolean shouldDispenseBoatsAbove = false;
        private boolean canSwim = false;
        private BiFunction<Entity, Double, Float> fluidMovementSpeed = (entity, aDouble) -> 0.0f;
        private boolean applyWaterMovement = false;
        private Consumer<ItemEntity> applyBuoyancy = null;
        private boolean canCauseDrowning = false;
        private boolean canDrownedSpawn = false;
        private boolean canEndermanTeleportInto = false;
        private boolean canGuardianSpawn = false;
        private boolean useSkeletonHorseSubmergedSound = false;
        private boolean canWaterCreatureSpawn = false;
        private boolean shouldWitchDrinkWaterBreathing = false;
        private boolean willZombiesConvert = false;
        private boolean canTropicalFishSpawn = false;
        private boolean shouldTurtlesFavor = false;
        private boolean shouldTurtleHelmetActivate = false;
        private boolean affectsBlockBreakSpeed = false;
        private boolean canBoatsWork = false;
        private boolean shouldEvaporateInUltrawarm = false;
        private ParticleOptions bubbleParticle = ParticleTypes.BUBBLE;
        private ParticleOptions splashParticle = ParticleTypes.SPLASH;
        private boolean shouldBreakLanding = true;
        private boolean shouldExtinguish = false;

        public Builder(@NotNull TagKey<Fluid> fluidTag) {
            this.fluidTag = fluidTag;
        }

        public Builder hardensConcrete() {
            this.hardensConcrete = true;
            return this;
        }

        public Builder canCoralSurvive() {
            this.canCoralSurvive = true;
            return this;
        }

        public Builder canMoisturizeFarmland() {
            this.canMoisturizeFarmland = true;
            return this;
        }

        public Builder canKelpSurvive() {
            this.canKelpSurvive = true;
            return this;
        }

        public Builder affectsRespawnAnchor() {
            this.affectsRespawnAnchor = true;
            return this;
        }

        public Builder canSeagrassSurvive() {
            this.canSeagrassSurvive = true;
            return this;
        }

        public Builder canSpongeAbsorb() {
            this.canSpongeAbsorb = true;
            return this;
        }

        public Builder preventsBlockSpreading() {
            this.preventsBlockSpreading = true;
            return this;
        }

        public Builder canSugarCaneUse() {
            this.canSugarCaneUse = true;
            return this;
        }

        public Builder shouldDispenseBoatsAbove() {
            this.shouldDispenseBoatsAbove = true;
            return this;
        }

        public Builder canSwim() {
            this.canSwim = true;
            return this;
        }

        public Builder fluidMovementSpeed(@NotNull BiFunction<Entity, Double, Float> fluidMovementSpeed) {
            this.fluidMovementSpeed = fluidMovementSpeed;
            return this;
        }

        public Builder applyWaterMovement() {
            this.applyWaterMovement = true;
            return this;
        }

        public Builder applyBuoyancy(@NotNull Consumer<ItemEntity> applyBuoyancy) {
            this.applyBuoyancy = applyBuoyancy;
            return this;
        }

        public Builder canCauseDrowning() {
            this.canCauseDrowning = true;
            return this;
        }

        public Builder canDrownedSpawn() {
            this.canDrownedSpawn = true;
            return this;
        }

        public Builder canEndermanTeleportInto() {
            this.canEndermanTeleportInto = true;
            return this;
        }

        public Builder canGuardianSpawn() {
            this.canGuardianSpawn = true;
            return this;
        }

        public Builder useSkeletonHorseSubmergedSound() {
            this.useSkeletonHorseSubmergedSound = true;
            return this;
        }

        public Builder canWaterCreatureSpawn() {
            this.canWaterCreatureSpawn = true;
            return this;
        }

        public Builder shouldWitchDrinkWaterBreathing() {
            this.shouldWitchDrinkWaterBreathing = true;
            return this;
        }

        public Builder willZombiesConvert() {
            this.willZombiesConvert = true;
            return this;
        }

        public Builder canTropicalFishSpawn() {
            this.canTropicalFishSpawn = true;
            return this;
        }

        public Builder shouldTurtlesFavor() {
            this.shouldTurtlesFavor = true;
            return this;
        }

        public Builder shouldTurtleHelmetActivate() {
            this.shouldTurtleHelmetActivate = true;
            return this;
        }

        public Builder affectsBlockBreakSpeed() {
            this.affectsBlockBreakSpeed = true;
            return this;
        }

        public Builder canBoatsWork() {
            this.canBoatsWork = true;
            return this;
        }

        public Builder shouldEvaporateInUltrawarm() {
            this.shouldEvaporateInUltrawarm = true;
            return this;
        }

        public Builder bubbleParticle(@NotNull ParticleOptions bubbleParticle) {
            this.bubbleParticle = bubbleParticle;
            return this;
        }

        public Builder splashParticle(@NotNull ParticleOptions splashParticle) {
            this.splashParticle = splashParticle;
            return this;
        }

        public Builder shouldNotBreakLanding() {
            this.shouldBreakLanding = false;
            return this;
        }

        public Builder shouldExtinguish() {
            this.shouldExtinguish = true;
            return this;
        }

        public FluidData build() {
            return new FluidData(this.fluidTag, this.hardensConcrete, this.canCoralSurvive, this.canMoisturizeFarmland,
                    this.canKelpSurvive, this.affectsRespawnAnchor, this.canSeagrassSurvive, this.canSpongeAbsorb,
                    this.preventsBlockSpreading, this.canSugarCaneUse, this.shouldDispenseBoatsAbove, this.canSwim,
                    this.fluidMovementSpeed, this.applyWaterMovement, this.applyBuoyancy, this.canCauseDrowning,
                    this.canDrownedSpawn, this.canEndermanTeleportInto, this.canGuardianSpawn,
                    this.useSkeletonHorseSubmergedSound, this.canWaterCreatureSpawn, this.shouldWitchDrinkWaterBreathing,
                    this.willZombiesConvert, this.canTropicalFishSpawn, this.shouldTurtlesFavor,
                    this.shouldTurtleHelmetActivate, this.affectsBlockBreakSpeed, this.canBoatsWork,
                    this.shouldEvaporateInUltrawarm, this.bubbleParticle, this.splashParticle, this.shouldBreakLanding,
                    this.shouldExtinguish);
        }
    }
}
