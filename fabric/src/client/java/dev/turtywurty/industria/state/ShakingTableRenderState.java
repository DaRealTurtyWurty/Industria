package dev.turtywurty.industria.state;

import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class ShakingTableRenderState extends IndustriaBlockEntityRenderState {
    public float recipeFrequency = 1.0f;
    public int progress = 0;
    public int maxProgress = 100;
    public AABB shakeBox;
    public ItemStack processingStack = ItemStack.EMPTY;
    public SingleFluidStorage inputFluidTank;
    public float shakeOffset = 0.0f;

    public ShakingTableRenderState() {
        super(1);
    }
}
