package dev.turtywurty.industria.state;

import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class MixerRenderState extends IndustriaBlockEntityRenderState {
    public boolean isMixing = false;
    public int progress = 0;
    public int maxProgress = 0;
    public SimpleContainer inputInventory;
    public List<Vec3> mixingItemPositions;
    public SyncingFluidStorage fluidTank;
    public float stirringRotation;
    public boolean hasItemInputConnection;
    public boolean hasItemOutputConnection;

    public MixerRenderState() {
        super(6);
    }
}
