package dev.turtywurty.industria.state;

import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;

public class CentrifugalConcentratorRenderState extends IndustriaBlockEntityRenderState {
    public int recipeRPM = 0;
    public int progress = 0;
    public int maxProgress = 0;

    public SyncingFluidStorage inputFluidTank;

    public float bowlRotation = 0f;

    public CentrifugalConcentratorRenderState() {
        super(1);
    }
}
