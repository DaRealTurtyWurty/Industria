package dev.turtywurty.industria.state;

import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

public class ClarifierRenderState extends IndustriaBlockEntityRenderState {
    public SimpleInventory outputInventory;
    public ItemStack nextOutputStack = ItemStack.EMPTY;
    public int progress = 0;
    public int maxProgress = 0;
    public SingleFluidStorage inputFluidTank;
    public SingleFluidStorage outputFluidTank;

    public ClarifierRenderState() {
        super(2);
    }
}
