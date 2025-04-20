package dev.turtywurty.industria.blockentity.util.energy;

import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class WrappedEnergyStorage extends WrappedStorage<EnergyStorage> {
    @Override
    public NbtList writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var list = new NbtList();
        for (EnergyStorage storage : this.storages) {
            var nbt = new NbtCompound();
            nbt.putLong("Amount", storage.getAmount());
            list.add(nbt);
        }

        return list;
    }

    @Override
    public void readNbt(NbtList nbt, RegistryWrapper.WrapperLookup registryLookup) {
        for (int index = 0; index < nbt.size(); index++) {
            NbtCompound compound = nbt.getCompoundOrEmpty(index);
            EnergyStorage storage = this.storages.get(index);
            long amount = compound.getLong("Amount", 0L);
            if (storage instanceof SimpleEnergyStorage simpleEnergyStorage) {
                simpleEnergyStorage.amount = amount;
            } else {
                try (Transaction transaction = Transaction.openOuter()) {
                    long current = storage.getAmount();
                    if (current < amount) {
                        storage.insert(amount - current, transaction);
                    } else if (current > amount) {
                        storage.extract(current - amount, transaction);
                    }

                    transaction.commit();
                }
            }
        }
    }
}
