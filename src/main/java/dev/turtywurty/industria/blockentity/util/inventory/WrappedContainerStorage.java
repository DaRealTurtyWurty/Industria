package dev.turtywurty.industria.blockentity.util.inventory;

import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import dev.turtywurty.industria.util.ViewSerializable;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class WrappedContainerStorage<T extends SimpleContainer> extends WrappedStorage<ContainerStorage> {
    private final List<T> inventories = new ArrayList<>();
    private final List<Tuple<Direction, T>> sidedInventories = new ArrayList<>();
    private final CombinedStorage<ItemVariant, ContainerStorage> combinedStorage = new CombinedStorage<>(this.storages);

    public void addInventory(@NotNull T inventory) {
        addInventory(inventory, null);
    }

    public void addInventory(@NotNull T inventory, Direction side) {
        this.inventories.add(inventory);
        this.sidedInventories.add(new Tuple<>(side, inventory));
        var storage = ContainerStorage.of(inventory, side);
        addStorage(storage, side);
    }

    public void addInventory(@NotNull T inventory, BooleanSupplier canInsert, BooleanSupplier canExtract) {
        addInventory(inventory, null, canInsert, canExtract);
    }

    public void addInsertOnlyInventory(@NotNull T inventory, Direction side) {
        addInsertOnlyInventory(inventory, side, () -> true);
    }

    public void addInsertOnlyInventory(@NotNull T inventory, Direction side, BooleanSupplier canInsert) {
        addInventory(inventory, side, canInsert, () -> false);
    }

    public void addExtractOnlyInventory(@NotNull T inventory, Direction side) {
        addInventory(inventory, side, () -> false, () -> true);
    }

    public void addExtractOnlyInventory(@NotNull T inventory, Direction side, BooleanSupplier canExtract) {
        addInventory(inventory, side, () -> false, canExtract);
    }

    public void addInventory(@NotNull T inventory, Direction side, BooleanSupplier canInsert, BooleanSupplier canExtract) {
        this.inventories.add(inventory);
        this.sidedInventories.add(new Tuple<>(side, inventory));
        var storage = PredicateContainerStorage.of(ContainerStorage.of(inventory, side), canInsert, canExtract);
        addStorage(storage, side);
    }

    public List<T> getInventories() {
        return inventories;
    }

    public CombinedStorage<ItemVariant, ContainerStorage> getCombinedStorage() {
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
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                stacks.add(inventory.getItem(i));
            }
        }

        return stacks;
    }

    public void checkSize(int size) {
        if (this.inventories.stream().map(Container::getContainerSize).reduce(0, Integer::sum) != size)
            throw new IllegalArgumentException("Size of inventories does not match the size provided: " + size);
    }

    public void onOpen(@NotNull Player player) {
        for (T inventory : this.inventories) {
            inventory.startOpen(player);
        }
    }

    public void onClose(@NotNull Player player) {
        for (T inventory : this.inventories) {
            inventory.stopOpen(player);
        }
    }

    public void dropContents(@NotNull Level world, @NotNull BlockPos pos) {
        for (T inventory : this.inventories) {
            Containers.dropContents(world, pos, inventory);
        }
    }

    public RecipeSimpleInventory getRecipeInventory() {
        return new RecipeSimpleInventory(getStacks().toArray(new ItemStack[0]));
    }

    public List<Tuple<Direction, T>> getSidedInventories() {
        return this.sidedInventories;
    }

    @Override
    public void writeData(ValueOutput view) {
        for (int i = 0; i < this.inventories.size(); i++) {
            T inventory = this.inventories.get(i);
            ViewUtils.putChild(view, "Inventory_" + i, new SimpleInventorySerializer<>(inventory));
        }
    }

    @Override
    public void readData(ValueInput view) {
        for (int i = 0; i < this.inventories.size(); i++) {
            T inventory = this.inventories.get(i);
            ViewUtils.readChild(view, "Inventory_" + i, new SimpleInventorySerializer<>(inventory));
        }
    }

    public record SimpleInventorySerializer<T extends SimpleContainer>(T inventory) implements ViewSerializable {
        @Override
        public void writeData(ValueOutput view) {
            ContainerHelper.saveAllItems(view, this.inventory.getItems());
        }

        @Override
        public void readData(ValueInput view) {
            ContainerHelper.loadAllItems(view, this.inventory.getItems());
        }
    }
}
