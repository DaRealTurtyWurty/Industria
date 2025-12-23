package dev.turtywurty.industria.state;

import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;

public class ShakingTableRenderState extends IndustriaBlockEntityRenderState {
    public float recipeFrequency = 1.0f;
    public int progress = 0;
    public int maxProgress = 100;
    public Box shakeBox;
    public ItemStack processingStack = ItemStack.EMPTY;
    public SingleFluidStorage inputFluidTank;

    public ShakingTableRenderState() {
        super(1);
    }
}
