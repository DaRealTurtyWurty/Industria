package dev.turtywurty.industria.blockentity.util.energy;

import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class WrappedEnergyStorage extends WrappedStorage<EnergyStorage> {

    @Override
    public void writeData(WriteView view) {
        for (EnergyStorage storage : this.storages) {
            view.putLong("Amount", storage.getAmount());
        }
    }

    @Override
    public void readData(ReadView view) {
        for (EnergyStorage storage : this.storages) {
            long amount = view.getLong("Amount", 0L);
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
