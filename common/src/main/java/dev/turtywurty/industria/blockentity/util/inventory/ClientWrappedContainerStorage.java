package dev.turtywurty.industria.blockentity.util.inventory;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleContainer;

public class ClientWrappedContainerStorage extends WrappedContainerStorage<SimpleContainer> {
    public static ClientWrappedContainerStorage copyOf(WrappedContainerStorage<?> inventory) {
        var wrappedContainerStorage = new ClientWrappedContainerStorage();
        for (Pair<Direction, ? extends SimpleContainer> entry : inventory.getSidedInventories()) {
            wrappedContainerStorage.addInventory(new SimpleContainer(entry.getSecond().getContainerSize()), entry.getFirst());
        }

        return wrappedContainerStorage;
    }
}
