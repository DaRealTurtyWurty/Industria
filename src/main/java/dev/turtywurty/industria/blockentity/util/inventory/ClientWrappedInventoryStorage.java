package dev.turtywurty.industria.blockentity.util.inventory;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Direction;

public class ClientWrappedInventoryStorage extends WrappedInventoryStorage<SimpleInventory> {
    public static ClientWrappedInventoryStorage copyOf(WrappedInventoryStorage<?> inventory) {
        var wrappedInventoryStorage = new ClientWrappedInventoryStorage();
        for (Pair<Direction, ? extends SimpleInventory> entry : inventory.getSidedInventories()) {
            wrappedInventoryStorage.addInventory(new SimpleInventory(entry.getRight().size()), entry.getLeft());
        }

        return wrappedInventoryStorage;
    }
}
