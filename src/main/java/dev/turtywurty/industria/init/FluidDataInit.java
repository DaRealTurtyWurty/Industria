package dev.turtywurty.industria.init;

import dev.turtywurty.industria.fluid.FluidData;
import dev.turtywurty.industria.init.list.TagList;
import net.minecraft.particle.ParticleTypes;

public class FluidDataInit {
    public static void init() {
        // TODO: Change particles
        var commonFluidData = new FluidData.Builder(TagList.Fluids.CRUDE_OIL)
                .preventsBlockSpreading()
                .canSwim()
                .fluidMovementSpeed((entity, speed) -> 0.01F)
                .applyWaterMovement()
                .applyBuoyancy(itemEntity -> itemEntity.setVelocity(itemEntity.getVelocity().add(0.0D, 0.01D, 0.0D)))
                .canCauseDrowning()
                .shouldWitchDrinkWaterBreathing()
                .affectsBlockBreakSpeed()
                .bubbleParticle(ParticleTypes.ASH)
                .splashParticle(ParticleTypes.HEART)
                .build();

        FluidData.registerFluidData(FluidInit.CRUDE_OIL.still(), commonFluidData);
        FluidData.registerFluidData(FluidInit.CRUDE_OIL.flowing(), commonFluidData);

        FluidData.registerFluidData(FluidInit.DIRTY_SODIUM_ALUMINATE.still(), commonFluidData);
        FluidData.registerFluidData(FluidInit.DIRTY_SODIUM_ALUMINATE.flowing(), commonFluidData);

        FluidData.registerFluidData(FluidInit.SODIUM_ALUMINATE.still(), commonFluidData);
        FluidData.registerFluidData(FluidInit.SODIUM_ALUMINATE.flowing(), commonFluidData);

        FluidData.registerFluidData(FluidInit.MOLTEN_ALUMINIUM.still(), commonFluidData);
        FluidData.registerFluidData(FluidInit.MOLTEN_ALUMINIUM.flowing(), commonFluidData);

        FluidData.registerFluidData(FluidInit.MOLTEN_CRYOLITE.still(), commonFluidData);
        FluidData.registerFluidData(FluidInit.MOLTEN_CRYOLITE.flowing(), commonFluidData);
    }
}
