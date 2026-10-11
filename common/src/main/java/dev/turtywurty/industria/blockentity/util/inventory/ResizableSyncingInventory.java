package dev.turtywurty.industria.blockentity.util.inventory;

import dev.turtywurty.industria.blockentity.util.UpdateableBlockEntityLike;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

public class ResizableSyncingInventory extends SyncingSimpleInventory {
    private final BiFunction<UpdateableBlockEntityLike, Integer, SyncingSimpleInventory> backingFactory;

    private SyncingSimpleInventory backing;
    private BiPredicate<Integer, ItemStack> insertionPredicate = (slot, stack) -> true;

    public ResizableSyncingInventory(UpdateableBlockEntityLike owner, int initialSize, BiFunction<UpdateableBlockEntityLike, Integer, SyncingSimpleInventory> backingFactory) {
        super(owner, 0);
        this.backingFactory = backingFactory;
        this.backing = this.backingFactory.apply(owner, initialSize);
    }

    public ResizableSyncingInventory(UpdateableBlockEntityLike owner, int initialSize) {
        this(owner, initialSize, SyncingSimpleInventory::new);
    }

    public void ensureSize(int requiredSize) {
        if (requiredSize <= backing.getContainerSize())
            return;

        SyncingSimpleInventory newBacking = backingFactory.apply(getBlockEntity(), requiredSize);
        for (int slot = 0; slot < backing.getContainerSize(); slot++) {
            newBacking.getItems().set(slot, this.backing.getItem(slot).copy());
        }

        this.backing = newBacking;
        setChanged();
    }

    public void setInsertionPredicate(BiPredicate<Integer, ItemStack> insertionPredicate) {
        this.insertionPredicate = insertionPredicate;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot >= 0 && slot < getContainerSize()
                && insertionPredicate.test(slot, stack)
                && backing.canPlaceItem(slot, stack);
    }

    @Override
    public boolean canAddItem(ItemStack itemStack) {
        if (itemStack.isEmpty())
            return false;

        for (int slot = 0; slot < getContainerSize(); slot++) {
            if (canPlaceItem(slot, itemStack)) {
                ItemStack existingStack = getItem(slot);
                if (existingStack.isEmpty()
                        || (existingStack.getCount() < getMaxStackSize(existingStack)
                        && ItemStack.isSameItemSameComponents(existingStack, itemStack)))
                    return true;
            }
        }

        return false;
    }

    @Override
    public ItemStack addItem(ItemStack itemStack) {
        if (itemStack.isEmpty())
            return ItemStack.EMPTY;

        ItemStack remaining = itemStack.copy();

        for (int slot = 0; slot < getContainerSize() && !remaining.isEmpty(); slot++) {
            if (!canPlaceItem(slot, remaining))
                continue;

            ItemStack existingStack = getItem(slot);
            if (existingStack.isEmpty()
                    || !ItemStack.isSameItemSameComponents(existingStack, remaining))
                continue;

            int space = getMaxStackSize(remaining) - existingStack.getCount();
            if (space <= 0)
                continue;

            int amount = Math.min(remaining.getCount(), space);
            existingStack.grow(amount);
            remaining.shrink(amount);
            setChanged();
        }

        for (int slot = 0; slot < getContainerSize() && !remaining.isEmpty(); slot++) {
            if (!canPlaceItem(slot, remaining) || !getItem(slot).isEmpty())
                continue;

            int amount = Math.min(remaining.getCount(), getMaxStackSize(remaining));
            if (amount <= 0)
                continue;

            setItem(slot, remaining.split(amount));
        }

        return remaining;
    }

    @Override
    public int getContainerSize() {
        return backing.getContainerSize();
    }

    @Override
    public ItemStack getItem(int slot) {
        return backing.getItem(slot);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return backing.getItems();
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        backing.setItem(slot, itemStack);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        return backing.removeItem(slot, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return backing.removeItemNoUpdate(slot);
    }

    @Override
    public ItemStack removeItemType(Item itemType, int count) {
        return backing.removeItemType(itemType, count);
    }

    @Override
    public List<ItemStack> removeAllItems() {
        return backing.removeAllItems();
    }

    @Override
    public void clearContent() {
        backing.clearContent();
    }

    @Override
    public boolean isEmpty() {
        return backing.isEmpty();
    }

    @Override
    public void fillStackedContents(StackedItemContents contents) {
        backing.fillStackedContents(contents);
    }

    @Override
    public int getMaxStackSize() {
        return backing.getMaxStackSize();
    }

    @Override
    public String toString() {
        return backing.toString();
    }
}
