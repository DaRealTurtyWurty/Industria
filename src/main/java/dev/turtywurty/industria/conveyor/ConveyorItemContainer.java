package dev.turtywurty.industria.conveyor;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ConveyorItemContainer implements Container, StackedContentsCompatible {
    private final int itemLimit;
    private final List<ConveyorItem> items;

    public ConveyorItemContainer(int itemLimit, List<ConveyorItem> stacks) {
        if (itemLimit < 0)
            throw new IllegalArgumentException("Item limit cannot be negative");

        this.itemLimit = itemLimit;

        if (stacks == null)
            throw new IllegalArgumentException("Stacks cannot be null");

        if (stacks.size() > itemLimit)
            throw new IllegalArgumentException("Stacks length cannot be greater than item limit");

        this.items = stacks;
    }

    @Override
    public boolean canTakeItem(Container into, int slot, ItemStack itemStack) {
        return false;
    }

    @Override
    public int getContainerSize() {
        return itemLimit;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.size() > slot ? this.items.get(slot).getStack() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ConveyorItem removed = this.items.get(slot);
        if (removed == null)
            return ItemStack.EMPTY;

        if (removed.getStack().getCount() <= count) {
            this.items.remove(slot);
            return removed.getStack();
        } else {
            ItemStack split = removed.getStack().split(count);
            if (removed.getStack().isEmpty())
                this.items.remove(slot);

            return split;
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ConveyorItem removed = this.items.get(slot);
        if (removed == null)
            return ItemStack.EMPTY;

        this.items.remove(slot);
        return removed.getStack();
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        this.items.get(slot).setStack(itemStack);
    }

    @Override
    public void setChanged() {
        // NO-OP
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack itemStack) {
        return false;
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    public void fillStackedContents(StackedItemContents contents) {
        for (ConveyorItem item : items) {
            contents.accountStack(item.getStack());
        }
    }
}
