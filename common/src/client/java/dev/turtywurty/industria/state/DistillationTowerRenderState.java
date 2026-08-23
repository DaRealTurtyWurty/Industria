package dev.turtywurty.industria.state;

import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;

public class DistillationTowerRenderState extends IndustriaBlockEntityRenderState {
    public int progress;
    public int maxProgress;
    public SyncingFluidStorage inputFluidTank;
    public SyncingFluidStorage primaryOutputFluidTank;
    public SyncingFluidStorage secondaryOutputFluidTank;

    public DistillationTowerRenderState() {
        super(0);
    }
}
