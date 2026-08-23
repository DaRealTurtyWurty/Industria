package dev.turtywurty.industria.blockentity.util.inventory;

import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.SimpleContainer;

public class ClientWrappedContainerStorage extends WrappedContainerStorage<SimpleContainer> {
    public static ClientWrappedContainerStorage copyOf(WrappedContainerStorage<?> inventory) {
        var wrappedContainerStorage = new ClientWrappedContainerStorage();
        for (Tuple<Direction, ? extends SimpleContainer> entry : inventory.getSidedInventories()) {
            wrappedContainerStorage.addInventory(new SimpleContainer(entry.getB().getContainerSize()), entry.getA());
        }

        return wrappedContainerStorage;
    }
}
