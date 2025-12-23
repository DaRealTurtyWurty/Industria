package dev.turtywurty.industria.state;

import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;

public class FluidTankRenderState extends IndustriaBlockEntityRenderState {
    public SingleFluidStorage fluidTank;

    public FluidTankRenderState() {
        super(0);
    }
}
