package dev.turtywurty.industria.state;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;

public class DrillRenderState extends IndustriaBlockEntityRenderState {
    public SimpleInventory motorInventory;
    public ItemStack drillHeadItemStack;
    public boolean isDrilling;
    public boolean isRetracting;
    public float drillYOffset;
    public Box drillHeadAABB;
    public boolean isPaused;

    public float clientMotorRotation;
    public float clockwiseRotation;
    public float counterClockwiseRotation;

    public DrillRenderState() {
        super(0);
    }
}
