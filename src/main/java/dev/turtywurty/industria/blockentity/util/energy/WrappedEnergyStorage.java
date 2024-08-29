package dev.turtywurty.industria.blockentity.util.energy;

import dev.turtywurty.industria.util.NBTSerializable;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WrappedEnergyStorage implements NBTSerializable<NbtList> {
    private final List<SimpleEnergyStorage> storages = new ArrayList<>();
    private final Map<Direction, SimpleEnergyStorage> sidedStorageMap = new HashMap<>();

    public void addStorage(SimpleEnergyStorage storage) {
        addStorage(storage, null);
    }

    public void addStorage(SimpleEnergyStorage storage, Direction side) {
        this.storages.add(storage);

        if(side == null) {
            for (Direction direction : Direction.values()) {
                this.sidedStorageMap.put(direction, storage);
            }
        } else {
            this.sidedStorageMap.put(side, storage);
        }
    }

    public List<SimpleEnergyStorage> getStorages() {
        return storages;
    }

    public Map<Direction, SimpleEnergyStorage> getSidedStorageMap() {
        return sidedStorageMap;
    }

    public SimpleEnergyStorage getStorage(@Nullable Direction side) {
        if(side == null)
            return this.storages.getFirst();

        return this.sidedStorageMap.get(side);
    }

    @Override
    public NbtList writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var list = new NbtList();
        for (SimpleEnergyStorage storage : this.storages) {
            var nbt = new NbtCompound();
            nbt.putLong("Amount", storage.getAmount());
            list.add(nbt);
        }

        return list;
    }

    @Override
    public void readNbt(NbtList nbt, RegistryWrapper.WrapperLookup registryLookup) {
        for (int index = 0; index < nbt.size(); index++) {
            var compound = nbt.getCompound(index);
            this.storages.get(index).amount = compound.getLong("Amount");
        }
    }
}
