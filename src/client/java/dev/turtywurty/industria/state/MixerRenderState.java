package dev.turtywurty.industria.state;

import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class MixerRenderState extends IndustriaBlockEntityRenderState {
    public float stirringRotation = 0.0f;
    public boolean isMixing = false;
    public int progress = 0;
    public int maxProgress = 0;
    public SimpleInventory inputInventory;
    public List<Vec3d> mixingItemPositions;
    public SingleFluidStorage fluidTank;

    public MixerRenderState() {
        super(1);
    }
}
