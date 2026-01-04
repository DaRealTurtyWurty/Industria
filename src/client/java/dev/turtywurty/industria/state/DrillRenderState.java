package dev.turtywurty.industria.state;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class DrillRenderState extends IndustriaBlockEntityRenderState {
    public SimpleContainer motorInventory;
    public ItemStack drillHeadItemStack;
    public boolean isDrilling;
    public boolean isRetracting;
    public float drillYOffset;
    public AABB drillHeadAABB;
    public boolean isPaused;

    public float clientMotorRotation;
    public float clockwiseRotation;
    public float counterClockwiseRotation;
    public float cableScaleFactor;

    public DrillRenderState() {
        super(0);
    }
}
