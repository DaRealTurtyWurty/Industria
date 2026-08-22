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
                .applyBuoyancy(itemEntity -> itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add(0.0D, 0.01D, 0.0D)))
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

        FluidData.registerFluidData(ModFluids.MOLTEN_ALUMINIUM.still().get(), commonFluidData);
        FluidData.registerFluidData(ModFluids.MOLTEN_ALUMINIUM.flowing().get(), commonFluidData);

        FluidData.registerFluidData(ModFluids.MOLTEN_CRYOLITE.still().get(), commonFluidData);
        FluidData.registerFluidData(ModFluids.MOLTEN_CRYOLITE.flowing().get(), commonFluidData);
    }
}
