package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.ChargingHatchBlockEntity;
import dev.turtywurty.industria.blockentity.util.inventory.ClientWrappedContainerStorage;
import dev.turtywurty.industria.blockentity.util.inventory.WrappedContainerStorage;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.menu.base.IndustriaScreenHandler;
import dev.turtywurty.industria.network.ChargingHatchOpenPayload;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class ChargingHatchMenu extends IndustriaScreenHandler<ChargingHatchBlockEntity, ChargingHatchOpenPayload> {
    private int scrollRow;

    public ChargingHatchMenu(int syncId, Inventory playerInventory, ChargingHatchOpenPayload payload) {
        this(
            syncId,
            playerInventory,
            findBlockEntity(playerInventory, payload),
            createClientInventory(payload.slotCount()),
            new SimpleContainerData(payload.slotCount() + 1));
    }

    public ChargingHatchMenu(
        int syncId,
        Inventory playerInventory,
        ChargingHatchBlockEntity blockEntity,
        WrappedContainerStorage<?> wrappedContainerStorage,
        ContainerData menuData
    ) {
        super(ModMenuTypes.CHARGING_HATCH.get(), syncId, playerInventory, blockEntity, wrappedContainerStorage,
            menuData);
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        SimpleContainer inventory = Objects.requireNonNull(this.wrappedContainerStorage.getInventory(0));

        for (int index = 0; index < getInventorySize(); index++) {
            addSlot(createHatchSlot(inventory, index));
        }
    }

    public void setScrollRow(int row) {
        row = Math.clamp(row, 0, getMaxScrollRow());
        if (this.scrollRow == row)
            return;

        this.scrollRow = row;
        for (int index = 0; index < getInventorySize(); index++) {
            Slot replacement = createHatchSlot(
                Objects.requireNonNull(this.wrappedContainerStorage.getInventory(0)),
                index);
            replacement.index = index;
            this.slots.set(index, replacement);
        }
    }

    public int getDisplayedSlotCount() {
        SimpleContainer inventory = this.wrappedContainerStorage.getInventory(0);
        for (int index = getHatchSlotCount() - 1; index >= getProcessingSlotCount(); index--) {
            if (inventory.getItem(index).isEmpty())
                continue;

            return index + 1;
        }

        return getProcessingSlotCount();
    }

    private @NonNull Slot createHatchSlot(SimpleContainer inventory, int index) {
        int x = 62 + (index % 3) * 18;
        int y = 17 + (index / 3 - this.scrollRow) * 18;
        return new Slot(inventory, index, x, y) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return getContainerSlot() < ChargingHatchMenu.this.getProcessingSlotCount()
                    && super.mayPlace(itemStack);
            }

            @Override
            public boolean isActive() {
                return !ChargingHatchMenu.this.blockEntity.getLevel().isClientSide()
                    || ChargingHatchMenu.this.isSlotVisible(getContainerSlot());
            }
        };
    }

    @Override
    protected int getInventorySize() {
        return this.propertyDelegate.getCount() - 1;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.context, player, ModBlocks.CHARGING_HATCH.get())
            && this.blockEntity.getInventory().getContainerSize() == getInventorySize();
    }

    public float getMeltingProgress(int slot) {
        return this.propertyDelegate.get(slot + 1) / 1000.0F;
    }

    public int getProcessingSlotCount() {
        return this.propertyDelegate.get(0);
    }

    public int getHatchSlotCount() {
        return getInventorySize();
    }

    public int getScrollRow() {
        return this.scrollRow;
    }

    public int getMaxScrollRow() {
        return Math.max(0, (getDisplayedSlotCount() + 2) / 3 - 3);
    }

    public boolean isSlotVisible(int slot) {
        if (slot >= getProcessingSlotCount()) {
            ItemStack stack = this.wrappedContainerStorage.getInventory(0).getItem(slot);
            if (stack.isEmpty())
                return false;
        }

        int row = slot / 3;
        return row >= this.scrollRow && row < this.scrollRow + 3;
    }

    private static ClientWrappedContainerStorage createClientInventory(int size) {
        var storage = new ClientWrappedContainerStorage();
        storage.addInventory(new SimpleContainer(size));
        return storage;
    }

    private static ChargingHatchBlockEntity findBlockEntity(Inventory inventory, ChargingHatchOpenPayload payload) {
        if (inventory.player.level().getBlockEntity(payload.pos()) instanceof ChargingHatchBlockEntity hatch)
            return hatch;

        throw new IllegalStateException("Missing charging hatch at " + payload.pos());
    }
}
