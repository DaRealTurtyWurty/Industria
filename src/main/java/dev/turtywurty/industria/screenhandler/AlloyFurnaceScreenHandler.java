package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.AlloyFurnaceBlockEntity;
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

public class AlloyFurnaceScreenHandler extends IndustriaScreenHandler<AlloyFurnaceBlockEntity, BlockPosPayload> {
    public AlloyFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.ALLOY_FURNACE, 4, syncId, playerInventory, payload, AlloyFurnaceBlockEntity.class);
    }

    public AlloyFurnaceScreenHandler(int syncId, PlayerInventory playerInventory, AlloyFurnaceBlockEntity blockEntity,
                                     WrappedInventoryStorage<?> wrappedInventoryStorage, PropertyDelegate propertyDelegate) {
        super(ScreenHandlerTypeInit.ALLOY_FURNACE, syncId, playerInventory, blockEntity, wrappedInventoryStorage, propertyDelegate);
    }

    @Override
    protected void addBlockEntitySlots(PlayerInventory playerInventory) {
        WrappedInventoryStorage<?> inventory = this.wrappedInventoryStorage;
        addSlot(new Slot(inventory.getInventory(AlloyFurnaceBlockEntity.INPUT_SLOT_0), 0, 42, 17));
        addSlot(new Slot(inventory.getInventory(AlloyFurnaceBlockEntity.INPUT_SLOT_1), 0, 70, 17));
        addSlot(new PredicateSlot(inventory.getInventory(AlloyFurnaceBlockEntity.FUEL_SLOT), 0, 56, 53));
        addSlot(new OutputSlot(inventory.getInventory(AlloyFurnaceBlockEntity.OUTPUT_SLOT), 0, 116, 35));
    }

    @Override
    protected int getInventorySize() {
        return 4;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slotObject = this.slots.get(slot);

        SimpleInventory inputInventory = this.blockEntity.getInventory();

        if (slotObject.hasStack()) {
            ItemStack stackInSlot = slotObject.getStack();
            stack = stackInSlot.copy();

            if (slot < inputInventory.size()) {
                if (!insertItem(stackInSlot, inputInventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertItem(stackInSlot, 0, inputInventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slotObject.setStack(ItemStack.EMPTY);
            } else {
                slotObject.markDirty();
            }
        }

        return stack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, BlockInit.ALLOY_FURNACE);
    }

    public int getProgress() {
        return this.propertyDelegate.get(0);
    }

    public int getMaxProgress() {
        return this.propertyDelegate.get(1);
    }

    public int getBurnTime() {
        return this.propertyDelegate.get(2);
    }

    public int getMaxBurnTime() {
        return this.propertyDelegate.get(3);
    }

    public float getProgressPercent() {
        float progress = getProgress();
        float maxProgress = getMaxProgress();
        if (maxProgress == 0 || progress == 0)
            return 0.0F;

        return MathHelper.clamp(progress / maxProgress, 0.0F, 1.0F);
    }

    public float getBurnTimePercent() {
        float progress = getBurnTime();
        float maxProgress = getMaxBurnTime();
        if (maxProgress == 0 || progress == 0)
            return 0.0F;

        return MathHelper.clamp(progress / maxProgress, 0.0F, 1.0F);
    }
}
