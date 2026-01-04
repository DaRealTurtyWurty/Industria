package dev.turtywurty.industria.state;

import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MixerRenderState extends IndustriaBlockEntityRenderState {
    public boolean isMixing = false;
    public int progress = 0;
    public int maxProgress = 0;
    public SimpleContainer inputInventory;
    public List<Vec3> mixingItemPositions;
    public SingleFluidStorage fluidTank;

    public MixerRenderState() {
        super(6);
    }
}
