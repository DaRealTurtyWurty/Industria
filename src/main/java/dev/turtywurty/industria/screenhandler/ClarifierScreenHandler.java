package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.ClarifierBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import dev.turtywurty.industria.screenhandler.slot.OutputSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.MathHelper;

public class ClarifierScreenHandler extends IndustriaScreenHandler<ClarifierBlockEntity, BlockPosPayload> {
    public ClarifierScreenHandler(int syncId, PlayerInventory playerInv, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.CLARIFIER, 2, syncId, playerInv, payload, ClarifierBlockEntity.class);
    }

    public ClarifierScreenHandler(int syncId, PlayerInventory playerInv, ClarifierBlockEntity blockEntity, WrappedInventoryStorage<?> wrappedInventoryStorage, PropertyDelegate properties) {
        super(ScreenHandlerTypeInit.CLARIFIER, syncId, playerInv, blockEntity, wrappedInventoryStorage, properties);

        addPlayerSlots(playerInv, 8, 92);
    }

    @Override
    protected int getInventorySize() {
        return 1;
    }

    @Override
    protected int getPlayerInventoryY() {
        return 92;
    }

    @Override
    protected void addBlockEntitySlots(PlayerInventory playerInventory) {
        SimpleInventory inventory = this.wrappedInventoryStorage.getInventory(0);
        addSlot(new OutputSlot(inventory, 0, 134,60));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasStack()) {
            return stack;
        }

        ItemStack stackInSlot = slot.getStack();
        stack = stackInSlot.copy();

        if (slotIndex == 0) {
            if (!insertItem(stackInSlot, this.slots.size() - 9, this.slots.size(), true)) {
                if (!insertItem(stackInSlot, this.slots.size() - 36, this.slots.size() - 9, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else if (!insertItem(stackInSlot, 0, 0, false)) {
            return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        return stack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.CLARIFIER);
    }

    public int getProgress() {
        return this.propertyDelegate.get(0);
    }

    public int getMaxProgress() {
        return this.propertyDelegate.get(1);
    }

    public float getProgressPercent() {
        float progress = getProgress();
        float maxProgress = getMaxProgress();
        if (maxProgress == 0 || progress == 0)
            return 0.0F;

        return MathHelper.clamp(progress / maxProgress, 0.0F, 1.0F);
    }

    public int getProgressScaled() {
        return MathHelper.ceil(getProgressPercent() * 24);
    }
}
