package dev.turtywurty.industria.blockentity.util.energy;

import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import dev.turtywurty.industria.util.ViewSerializable;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public class WrappedEnergyStorage extends WrappedStorage<EnergyStorage> {
    @Override
    public void writeData(WriteView view) {
        for (int i = 0; i < this.storages.size(); i++) {
            EnergyStorage storage = this.storages.get(i);
            ViewUtils.putChild(view, "EnergyStorage_" + i, new EnergyStorageSerializer(storage));
        }
    }

    @Override
    public void readData(ReadView view) {
        for (int i = 0; i < this.storages.size(); i++) {
            EnergyStorage storage = this.storages.get(i);
            ViewUtils.readChild(view, "EnergyStorage_" + i, new EnergyStorageSerializer(storage));
        }
    }

    public record EnergyStorageSerializer(EnergyStorage storage) implements ViewSerializable {
        @Override
        public void writeData(WriteView view) {
            view.putLong("Amount", storage.getAmount());
        }

        @Override
        public void readData(ReadView view) {
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
