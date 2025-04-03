package dev.turtywurty.industria.blockentity.util.inventory;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.util.math.Direction;

import java.util.Map;

public class ClientWrappedInventoryStorage extends WrappedInventoryStorage<SimpleInventory> {
    public static ClientWrappedInventoryStorage copyOf(WrappedInventoryStorage<?> inventory) {
        var wrappedInventoryStorage = new ClientWrappedInventoryStorage();
        for (Map.Entry<Direction, ? extends SimpleInventory> entry : inventory.getSidedInventories().entrySet()) {
            wrappedInventoryStorage.addInventory(new SimpleInventory(entry.getValue().size()), entry.getKey());
        }

        return wrappedInventoryStorage;
    }
}
