package dev.turtywurty.industria.blockentity.util.inventory;

import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class WrappedInventoryStorage<T extends SimpleInventory> extends WrappedStorage<InventoryStorage> {
    private final List<T> inventories = new ArrayList<>();
    private final List<Pair<Direction, T>> sidedInventories = new ArrayList<>();
    private final CombinedStorage<ItemVariant, InventoryStorage> combinedStorage = new CombinedStorage<>(this.storages);

    public void addInventory(@NotNull T inventory) {
        addInventory(inventory, null);
    }

    public void addInventory(@NotNull T inventory, Direction side) {
        this.inventories.add(inventory);
        this.sidedInventories.add(new Pair<>(side, inventory));
        var storage = InventoryStorage.of(inventory, side);
        addStorage(storage, side);
    }

    public void addInventory(@NotNull T inventory, Supplier<Boolean> canInsert, Supplier<Boolean> canExtract) {
        addInventory(inventory, null, canInsert, canExtract);
    }

    public void addInsertOnlyInventory(@NotNull T inventory, Direction side) {
        addInsertOnlyInventory(inventory, side, () -> true);
    }

    public void addInsertOnlyInventory(@NotNull T inventory, Direction side, Supplier<Boolean> canInsert) {
        addInventory(inventory, side, canInsert, () -> false);
    }

    public void addExtractOnlyInventory(@NotNull T inventory, Direction side) {
        addInventory(inventory, side, () -> false, () -> true);
    }

    public void addExtractOnlyInventory(@NotNull T inventory, Direction side, Supplier<Boolean> canExtract) {
        addInventory(inventory, side, () -> false, canExtract);
    }

    public void addInventory(@NotNull T inventory, Direction side, Supplier<Boolean> canInsert, Supplier<Boolean> canExtract) {
        this.inventories.add(inventory);
        this.sidedInventories.add(new Pair<>(side, inventory));
        var storage = PredicateInventoryStorage.of(InventoryStorage.of(inventory, side), canInsert, canExtract);
        addStorage(storage, side);
    }

    public List<T> getInventories() {
        return inventories;
    }

    public CombinedStorage<ItemVariant, InventoryStorage> getCombinedStorage() {
        return combinedStorage;
    }

    public @Nullable T getInventory(int index) {
        return this.inventories.get(index);
    }

    public @Nullable T getInventory(Direction side) {
        return this.inventories.get(this.storages.indexOf(getStorage(side)));
    }

    public @NotNull List<ItemStack> getStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (T inventory : this.inventories) {
            for (int i = 0; i < inventory.size(); i++) {
                stacks.add(inventory.getStack(i));
            }
        }

        return stacks;
    }

    public void checkSize(int size) {
        if (this.inventories.stream().map(Inventory::size).reduce(0, Integer::sum) != size)
            throw new IllegalArgumentException("Size of inventories does not match the size provided: " + size);
    }

    public void onOpen(@NotNull PlayerEntity player) {
        for (T inventory : this.inventories) {
            inventory.onOpen(player);
        }
    }

    public void onClose(@NotNull PlayerEntity player) {
        for (T inventory : this.inventories) {
            inventory.onClose(player);
        }
    }

    public void dropContents(@NotNull World world, @NotNull BlockPos pos) {
        for (T inventory : this.inventories) {
            ItemScatterer.spawn(world, pos, inventory);
        }
    }

    public RecipeSimpleInventory getRecipeInventory() {
        return new RecipeSimpleInventory(getStacks().toArray(new ItemStack[0]));
    }

    public List<Pair<Direction, T>> getSidedInventories() {
        return this.sidedInventories;
    }

    @Override
    public NbtList writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = new NbtList();
        for (T inventory : this.inventories) {
            var inventoryNbt = new NbtCompound();
            nbt.add(Inventories.writeNbt(inventoryNbt, inventory.getHeldStacks(), registryLookup));
        }

        return nbt;
    }

    @Override
    public void readNbt(NbtList nbt, RegistryWrapper.WrapperLookup registryLookup) {
        for (int index = 0; index < nbt.size(); index++) {
            var inventoryNbt = nbt.getCompound(index);

            SimpleInventory inventory = this.inventories.get(index);
            Inventories.readNbt(inventoryNbt, inventory.getHeldStacks(), registryLookup);

        }
    }
}
