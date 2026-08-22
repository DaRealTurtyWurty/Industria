package dev.turtywurty.industria.state;

import dev.turtywurty.industria.blockentity.util.fluid.SyncingFluidStorage;
import net.minecraft.world.item.ItemStack;

public class CrystallizerRenderState extends IndustriaBlockEntityRenderState {
    public ItemStack nextOutputItemStack = ItemStack.EMPTY;
    public int progress = 0;
    public int maxProgress = 0;
    public SyncingFluidStorage crystalFluidStorage;
    public SyncingFluidStorage waterFluidStorage;

    public CrystallizerRenderState() {
        super(1);
    }
}
