package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.ThermalGeneratorBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.base.IndustriaScreenHandler;
import dev.turtywurty.industria.screenhandler.slot.PredicateSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class ThermalGeneratorScreenHandler extends IndustriaScreenHandler<ThermalGeneratorBlockEntity, BlockPosPayload> {
    public ThermalGeneratorScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        super(ScreenHandlerTypeInit.THERMAL_GENERATOR, syncId, playerInventory, payload, ThermalGeneratorBlockEntity.class);
    }

    public ThermalGeneratorScreenHandler(int syncId, PlayerInventory playerInventory, ThermalGeneratorBlockEntity blockEntity, WrappedInventoryStorage<?> wrappedInventoryStorage) {
        super(ScreenHandlerTypeInit.THERMAL_GENERATOR, syncId, playerInventory, blockEntity, wrappedInventoryStorage);
    }

    @Override
    protected int getInventorySize() {
        return 1;
    }

    @Override
    protected void addBlockEntitySlots(PlayerInventory playerInventory) {
        addSlot(new PredicateSlot(this.wrappedInventoryStorage.getInventory(0), 0, 80, 35,
                itemStack -> this.blockEntity.isValid(itemStack, 0)));
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slotObject = this.slots.get(slot);

        if (slotObject.hasStack()) {
            ItemStack stackInSlot = slotObject.getStack();
            stack = stackInSlot.copy();

            if (slot < 0) {
                if (!insertItem(stackInSlot, 0, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!insertItem(stackInSlot, 0, 0, false)) {
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
        return canUse(this.context, player, BlockInit.THERMAL_GENERATOR);
    }

    public long getEnergy() {
        return this.blockEntity.getWrappedEnergyStorage().getAmount();
    }

    public long getMaxEnergy() {
        return this.blockEntity.getWrappedEnergyStorage().getCapacity();
    }

    public long getFluidAmount() {
        return this.blockEntity.getWrappedFluidStorage().getAmount();
    }

    public long getFluidCapacity() {
        return this.blockEntity.getWrappedFluidStorage().getCapacity();
    }

    public Fluid getFluid() {
        return this.blockEntity.getWrappedFluidStorage().variant.getFluid();
    }
}
