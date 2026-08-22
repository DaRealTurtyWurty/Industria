package dev.turtywurty.industria.state;

import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public class ShakingTableRenderState extends IndustriaBlockEntityRenderState {
    public float recipeFrequency = 1.0f;
    public int progress = 0;
    public int maxProgress = 100;
    public AABB shakeBox;
    public ItemStack processingStack = ItemStack.EMPTY;
    public SyncingFluidStorage inputFluidTank;
    public float shakeOffset = 0.0f;

    public ShakingTableRenderState() {
        super(1);
    }
}
