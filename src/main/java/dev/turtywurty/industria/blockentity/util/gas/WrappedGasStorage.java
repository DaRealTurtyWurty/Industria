package dev.turtywurty.industria.blockentity.util.gas;

import dev.turtywurty.gasapi.api.GasVariant;
import dev.turtywurty.gasapi.api.storage.SingleGasStorage;
import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

import java.util.ArrayList;
import java.util.List;

public class WrappedGasStorage<T extends Storage<GasVariant>> extends WrappedStorage<T> {
    private final CombinedStorage<GasVariant, T> combinedStorage = new CombinedStorage<>(this.storages);

    public CombinedStorage<GasVariant, T> getCombinedStorage() {
        return this.combinedStorage;
    }

    public List<GasStack> getSlurries() {
        List<GasStack> slurries = new ArrayList<>();
        for (T tank : this.storages) {
            for (StorageView<GasVariant> view : tank.nonEmptyViews()) {
                slurries.add(new GasStack(view.getResource(), view.getAmount()));
            }
        }

        return slurries;
    }

    @Override
    public void writeData(WriteView view) {
        for (T tank : this.storages) {
            if (tank instanceof SingleGasStorage singleGasStorage) {
                view.putLong("Amount", singleGasStorage.getAmount());
                view.put("Gas", GasVariant.CODEC, singleGasStorage.getResource());
            }
        }
    }

    @Override
    public void readData(ReadView view) {
        for (int index = 0; index < this.storages.size(); index++) {
            T storage = this.storages.get(index);
            if (storage == null)
                continue;

            if (storage instanceof SingleGasStorage singleGasStorage) {
                singleGasStorage.amount = view.getLong("Amount", 0L);
                singleGasStorage.variant = view.read("Gas", GasVariant.CODEC)
                        .orElse(GasVariant.blank());
            } else {
                throw new UnsupportedOperationException("Cannot read gas storage of type: " + storage.getClass().getName());
            }
        }
    }
}
