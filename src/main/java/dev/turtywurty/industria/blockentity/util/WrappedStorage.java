package dev.turtywurty.industria.blockentity.util;

import dev.turtywurty.industria.util.ViewSerializable;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class WrappedStorage<T> implements ViewSerializable {
    protected final List<T> storages = new ArrayList<>(Direction.values().length);
    protected final Map<Direction, T> sidedStorageMap = new HashMap<>(Direction.values().length);

    public void addStorage(T storage) {
        addStorage(storage, null);
    }

    public void addStorage(T storage, Direction side) {
        this.storages.add(storage);

        if (side == null) {
            for (Direction direction : Direction.values()) {
                this.sidedStorageMap.put(direction, storage);
            }
        } else {
            this.sidedStorageMap.put(side, storage);
        }
    }

    public List<T> getStorages() {
        return this.storages;
    }

    public Map<Direction, T> getSidedStorageMap() {
        return this.sidedStorageMap;
    }

    public T getStorage(Direction side) {
        if (side == null)
            return this.storages.getFirst();

        return this.sidedStorageMap.get(side);
    }

    public T getStorage(int index) {
        return this.storages.get(index);
    }
}
