package dev.turtywurty.industria.init;

import dev.turtywurty.industria.fluid.FluidData;
import dev.turtywurty.industria.init.list.TagList;
import net.minecraft.core.particles.ParticleTypes;

public class ModFluidData {
    public static void init() {
        // TODO: Change particles
        var commonFluidData = new FluidData.Builder(TagList.Fluids.CRUDE_OIL)
            .preventsBlockSpreading()
            .canSwim()
            .fluidMovementSpeed((_, _) -> 0.01F)
            .applyWaterMovement()
            .applyBuoyancy(
                itemEntity -> itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add(0.0D, 0.01D, 0.0D)))
            .canCauseDrowning()
            .shouldWitchDrinkWaterBreathing()
            .affectsBlockBreakSpeed()
            .bubbleParticle(ParticleTypes.ASH)
            .splashParticle(ParticleTypes.HEART)
            .build();

        FluidData.registerFluidData(ModFluids.CRUDE_OIL.still().get(), commonFluidData);
        FluidData.registerFluidData(ModFluids.CRUDE_OIL.flowing().get(), commonFluidData);

        FluidData.registerFluidData(ModFluids.DIRTY_SODIUM_ALUMINATE.still().get(), commonFluidData);
        FluidData.registerFluidData(ModFluids.DIRTY_SODIUM_ALUMINATE.flowing().get(), commonFluidData);

        FluidData.registerFluidData(ModFluids.SODIUM_ALUMINATE.still().get(), commonFluidData);
        FluidData.registerFluidData(ModFluids.SODIUM_ALUMINATE.flowing().get(), commonFluidData);

        ModFluids.getMoltenFluids().forEach((name, fluid) -> {
            var data = new FluidData.Builder(TagList.Fluids.of(name))
                .preventsBlockSpreading()
                .canSwim()
                .fluidMovementSpeed((_, _) -> 0.01F)
                .applyWaterMovement()
                .applyBuoyancy(entity -> entity.setDeltaMovement(entity.getDeltaMovement().add(0, 0.01, 0)))
                .canCauseDrowning()
                .shouldWitchDrinkWaterBreathing()
                .affectsBlockBreakSpeed()
                .bubbleParticle(ParticleTypes.ASH)
                .splashParticle(ParticleTypes.HEART)
                .build();
            FluidData.registerFluidData(fluid.still().get(), data);
            FluidData.registerFluidData(fluid.flowing().get(), data);
        });
    }
}
