package dev.turtywurty.industria.state;

import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.item.ItemStack;

public class CrystallizerRenderState extends IndustriaBlockEntityRenderState {
    public ItemStack nextOutputItemStack = ItemStack.EMPTY;
    public int progress = 0;
    public int maxProgress = 0;
    public SingleFluidStorage crystalFluidStorage;
    public SingleFluidStorage waterFluidStorage;

    public CrystallizerRenderState() {
        super(1);
    }
}
