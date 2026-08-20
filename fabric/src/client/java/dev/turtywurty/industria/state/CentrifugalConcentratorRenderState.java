package dev.turtywurty.industria.state;

import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;

public class CentrifugalConcentratorRenderState extends IndustriaBlockEntityRenderState {
    public int recipeRPM = 0;
    public int progress = 0;
    public int maxProgress = 0;

    public SingleFluidStorage inputFluidTank;

    public float bowlRotation = 0f;

    public CentrifugalConcentratorRenderState() {
        super(1);
    }
}
