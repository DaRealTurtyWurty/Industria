package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.CrusherBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import dev.turtywurty.industria.screenhandler.slot.OutputSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.MathHelper;

public class CrusherScreenHandler extends IndustriaScreenHandler<CrusherBlockEntity, BlockPosPayload> {
    public CrusherScreenHandler(int syncId, PlayerInventory playerInv, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.CRUSHER, 2, syncId, playerInv, payload, CrusherBlockEntity.class);
    }

    public CrusherScreenHandler(int syncId, PlayerInventory playerInv, CrusherBlockEntity blockEntity, WrappedInventoryStorage<?> wrappedInventoryStorage, PropertyDelegate propertyDelegate) {
        super(ScreenHandlerTypeInit.CRUSHER, syncId, playerInv, blockEntity, wrappedInventoryStorage, propertyDelegate);
    }

    @Override
    protected int getInventorySize() {
        return 3;
    }

    @Override
    protected void addBlockEntitySlots(PlayerInventory playerInventory) {
        WrappedInventoryStorage<?> wrappedStorage = this.wrappedInventoryStorage;
        addSlot(new Slot(wrappedStorage.getInventory(CrusherBlockEntity.INPUT_SLOT), 0, 44, 35));
        addSlot(new OutputSlot(wrappedStorage.getInventory(CrusherBlockEntity.OUTPUT_SLOT), 0, 98, 35));
        addSlot(new OutputSlot(wrappedStorage.getInventory(CrusherBlockEntity.OUTPUT_SLOT), 1, 116, 35));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if(slot.hasStack()) {
            ItemStack slotStack = slot.getStack().copy();

            if(slotIndex < 3) {
                if(!insertItem(slotStack, 3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertItem(slotStack, 0, 3, false)) {
                return ItemStack.EMPTY;
            }

            if(slotStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return itemStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.CRUSHER);
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
        if(maxProgress == 0 || progress == 0) {
            return 0.0f;
        }

        return MathHelper.clamp(progress / maxProgress, 0.0f, 1.0f);
    }

    public long getEnergy() {
        return this.blockEntity.getEnergy().getAmount();
    }

    public long getMaxEnergy() {
        return this.blockEntity.getEnergy().getCapacity();
    }

    public float getEnergyPercent() {
        long energy = getEnergy();
        long maxEnergy = getMaxEnergy();
        if(maxEnergy == 0 || energy == 0) {
            return 0.0f;
        }

        return MathHelper.clamp(energy / (float) maxEnergy, 0.0f, 1.0f);
    }
}
