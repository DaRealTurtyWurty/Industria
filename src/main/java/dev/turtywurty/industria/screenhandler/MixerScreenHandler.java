package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.MixerBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import dev.turtywurty.industria.screenhandler.slot.OutputSlot;
import dev.turtywurty.industria.screenhandler.slot.PredicateSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.MathHelper;

public class MixerScreenHandler extends IndustriaScreenHandler<MixerBlockEntity, BlockPosPayload> {
    public MixerScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.MIXER, 2, syncId, playerInventory, payload, MixerBlockEntity.class);
    }

    public MixerScreenHandler(int syncId, PlayerInventory playerInventory, MixerBlockEntity blockEntity, WrappedInventoryStorage<?> wrappedInventoryStorage, PropertyDelegate propertyDelegate) {
        super(ScreenHandlerTypeInit.MIXER, syncId, playerInventory, blockEntity, wrappedInventoryStorage, propertyDelegate);
    }

    @Override
    protected void addBlockEntitySlots(PlayerInventory playerInventory) {
        SimpleInventory inputInventory = this.wrappedInventoryStorage.getInventory(0);
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new Slot(inputInventory, column + (row * 3), 58 + column * 18, 26 + row * 18));
            }
        }

        addSlot(new OutputSlot(this.wrappedInventoryStorage.getInventory(1), 0, 143, 35));

        addSlot(new PredicateSlot(this.wrappedInventoryStorage.getInventory(2), 0, 36, 83));
        addSlot(new PredicateSlot(this.wrappedInventoryStorage.getInventory(3), 0, 170, 83));
    }

    @Override
    protected int getInventorySize() {
        return 9;
    }

    @Override
    protected int getPlayerInventoryX() {
        return 20;
    }

    @Override
    protected int getPlayerInventoryY() {
        return 112;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if(!slot.hasStack()) {
            return stack;
        }

        ItemStack stackInSlot = slot.getStack();
        stack = stackInSlot.copy();

        if(slotIndex < 9) {
            if(!insertItem(stackInSlot, this.slots.size() - 9, this.slots.size(), true)) {
                if(!insertItem(stackInSlot, this.slots.size() - 36, this.slots.size() - 9, false)) {
                    return ItemStack.EMPTY;
                }
            }
        } else {
            if(!insertItem(stackInSlot, 0, 9, false)) {
                return ItemStack.EMPTY;
            }
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
        return canUse(this.context, player, BlockInit.MIXER);
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
