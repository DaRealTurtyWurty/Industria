package dev.turtywurty.industria.screenhandler;

import dev.turtywurty.industria.blockentity.ThermalGeneratorBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedInventoryStorage;
import dev.turtywurty.industria.init.BlockInit;
import dev.turtywurty.industria.init.ScreenHandlerTypeInit;
import dev.turtywurty.industria.network.BlockPosPayload;
import dev.turtywurty.industria.screenhandler.slot.PredicateSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

public class ThermalGeneratorScreenHandler extends ScreenHandler {
    private final ThermalGeneratorBlockEntity blockEntity;
    private final ScreenHandlerContext context;

    public ThermalGeneratorScreenHandler(int syncId, PlayerInventory playerInventory, BlockPosPayload payload) {
        this(syncId, playerInventory, (ThermalGeneratorBlockEntity) playerInventory.player.getWorld().getBlockEntity(payload.pos()));
    }

    public ThermalGeneratorScreenHandler(int syncId, PlayerInventory playerInventory, ThermalGeneratorBlockEntity blockEntity) {
        super(ScreenHandlerTypeInit.THERMAL_GENERATOR, syncId);

        this.blockEntity = blockEntity;
        this.context = ScreenHandlerContext.create(blockEntity.getWorld(), blockEntity.getPos());

        WrappedInventoryStorage<SimpleInventory> inventory = blockEntity.getWrappedInventoryStorage();
        inventory.checkSize(1);
        inventory.onOpen(playerInventory.player);

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addBlockEntityInventory();
    }

    private void addPlayerInventory(PlayerInventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory inventory) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    private void addBlockEntityInventory() {
        addSlot(new PredicateSlot(this.blockEntity.getWrappedInventoryStorage().getInventory(0), 0, 80, 35,
                itemStack -> this.blockEntity.isValid(itemStack, 0)));
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.blockEntity.getWrappedInventoryStorage().onClose(player);
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

    public ThermalGeneratorBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    public long getEnergy() {
        return this.blockEntity.getEnergyStorage().getAmount();
    }

    public long getMaxEnergy() {
        return this.blockEntity.getEnergyStorage().getCapacity();
    }

    public long getFluidAmount() {
        return this.blockEntity.getFluidStorage().getAmount();
    }

    public long getFluidCapacity() {
        return this.blockEntity.getFluidStorage().getCapacity();
    }

    public Fluid getFluid() {
        return this.blockEntity.getFluidStorage().variant.getFluid();
    }
}
