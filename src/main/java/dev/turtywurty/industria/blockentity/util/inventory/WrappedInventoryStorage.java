package dev.turtywurty.industria.blockentity.util.inventory;

import dev.turtywurty.industria.util.NBTSerializable;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WrappedInventoryStorage<T extends SimpleInventory> implements NBTSerializable<NbtList> {
    private final List<T> inventories = new ArrayList<>();
    private final List<InventoryStorage> storages = new ArrayList<>();
    private final Map<Direction, InventoryStorage> sidedStorageMap = new HashMap<>();
    private final CombinedStorage<ItemVariant, InventoryStorage> combinedStorage = new CombinedStorage<>(this.storages);

    public void addInventory(@NotNull T inventory) {
        addInventory(inventory, null);
    }

    public void addInventory(@NotNull T inventory, Direction side) {
        this.inventories.add(inventory);
        var storage = InventoryStorage.of(inventory, side);
        this.storages.add(storage);
        this.sidedStorageMap.put(side, storage);
    }

    public List<T> getInventories() {
        return inventories;
    }

    public List<InventoryStorage> getStorages() {
        return storages;
    }

    public Map<Direction, InventoryStorage> getSidedStorageMap() {
        return sidedStorageMap;
    }

    public CombinedStorage<ItemVariant, InventoryStorage> getCombinedStorage() {
        return combinedStorage;
    }

    public @Nullable InventoryStorage getStorage(Direction side) {
        return this.sidedStorageMap.get(side);
    }

    public @Nullable T getInventory(int index) {
        return this.inventories.get(index);
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
