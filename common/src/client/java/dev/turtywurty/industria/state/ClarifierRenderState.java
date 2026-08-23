package dev.turtywurty.industria.state;

import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class ClarifierRenderState extends IndustriaBlockEntityRenderState {
    public SimpleContainer outputInventory;
    public ItemStack nextOutputStack = ItemStack.EMPTY;
    public int progress = 0;
    public int maxProgress = 0;
    public SyncingFluidStorage inputFluidTank;
    public SyncingFluidStorage outputFluidTank;

    public ClarifierRenderState() {
        super(2);
    }
}
