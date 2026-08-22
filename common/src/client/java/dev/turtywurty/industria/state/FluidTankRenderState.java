package dev.turtywurty.industria.state;

import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;

public class FluidTankRenderState extends IndustriaBlockEntityRenderState {
    public SyncingFluidStorage fluidTank;

    public FluidTankRenderState() {
        super(0);
    }
}
