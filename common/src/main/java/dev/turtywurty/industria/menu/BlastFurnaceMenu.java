package dev.turtywurty.industria.menu;

import dev.turtywurty.industria.blockentity.BlastFurnaceBlockEntity;
import dev.turtywurty.industria.init.ModBlocks;
import dev.turtywurty.industria.init.ModItems;
import dev.turtywurty.industria.init.ModMenuTypes;
import dev.turtywurty.industria.menu.base.IndustriaScreenHandler;
import dev.turtywurty.industria.menu.slot.OutputSlot;
import dev.turtywurty.industria.menu.slot.PredicateSlot;
import dev.turtywurty.industria.network.BlockPosPayload;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;

public class BlastFurnaceMenu extends IndustriaScreenHandler<BlastFurnaceBlockEntity, BlockPosPayload> {
    private static final int FUEL_SLOT = 2;
    private static final int SLOT_COUNT = 4;
    private static final int DATA_COUNT = 3;
    private static final int GAUGE_SCALE = 1000;

    public BlastFurnaceMenu(int syncId, Inventory inventory, BlockPosPayload payload) {
        super(ModMenuTypes.BLAST_FURNACE.get(), DATA_COUNT, syncId, inventory, payload, BlastFurnaceBlockEntity.class);
    }

    public BlastFurnaceMenu(int syncId, Inventory inventory, BlastFurnaceBlockEntity furnace) {
        super(ModMenuTypes.BLAST_FURNACE.get(), syncId, inventory, furnace, furnace.getWrappedContainerStorage(),
            new ContainerData() {
                @Override
                public int get(int index) {
                    return switch (index) {
                        case 0 -> Math.round(furnace.getBatchProgress() * GAUGE_SCALE);
                        case 1 -> Math.round(furnace.getBurnProgress() * GAUGE_SCALE);
                        case 2 -> furnace.hasConnectedBlower() ? 1 : 0;
                        default -> 0;
                    };
                }

                @Override
                public void set(int index, int value) {
                }

                @Override
                public int getCount() {
                    return DATA_COUNT;
                }
            });
    }

    @Override
    protected int getInventorySize() {
        return SLOT_COUNT;
    }

    @Override
    protected void addBlockEntitySlots(Inventory playerInventory) {
        SimpleContainer charge = this.wrappedContainerStorage.getInventory(Direction.UP);
        SimpleContainer fuel = this.wrappedContainerStorage.getInventory(Direction.NORTH);
        SimpleContainer slag = this.wrappedContainerStorage.getInventory(Direction.DOWN);
        addSlot(new Slot(charge, 0, 14, 36));
        addSlot(new Slot(charge, 1, 34, 36));
        addSlot(new PredicateSlot(fuel, 0, 65, 36, stack -> stack.is(ModItems.COAL_COKE.get())));
        addSlot(new OutputSlot(slag, 0, 116, 36));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < SLOT_COUNT)
            return super.quickMoveStack(player, index);

        Slot slot = this.slots.get(index);
        ItemStack stack = slot.getItem();
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        ItemStack original = stack.copy();
        boolean isCoke = stack.is(ModItems.COAL_COKE.get());
        if (!moveItemStackTo(stack, isCoke ? FUEL_SLOT : 0, isCoke ? FUEL_SLOT + 1 : FUEL_SLOT, false))
            return ItemStack.EMPTY;

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return !this.blockEntity.isRemoved() && stillValid(this.context, player, ModBlocks.BLAST_FURNACE.get());
    }

    public float getBatchProgress() {
        return this.propertyDelegate.get(0) / (float) GAUGE_SCALE;
    }

    public float getBurnProgress() {
        return this.propertyDelegate.get(1) / (float) GAUGE_SCALE;
    }

    public boolean hasConnectedBlower() {
        return this.propertyDelegate.get(2) != 0;
    }
}
