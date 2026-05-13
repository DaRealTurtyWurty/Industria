package dev.turtywurty.industria.state;

import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;

public class DistillationTowerRenderState extends IndustriaBlockEntityRenderState {
    public int progress;
    public int maxProgress;
    public SingleFluidStorage inputFluidTank;
    public SingleFluidStorage primaryOutputFluidTank;
    public SingleFluidStorage secondaryOutputFluidTank;

    public DistillationTowerRenderState() {
        super(0);
    }
}
