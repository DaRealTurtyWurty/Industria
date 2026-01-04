package dev.turtywurty.industria.blockentity.util.gas;

import dev.turtywurty.gasapi.api.GasVariant;
import dev.turtywurty.gasapi.api.storage.SingleGasStorage;
import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import dev.turtywurty.industria.util.ViewSerializable;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
    public void writeData(ValueOutput view) {
        for (int i = 0; i < this.storages.size(); i++) {
            T storage = this.storages.get(i);
            if (storage == null)
                continue;

            ViewUtils.putChild(view, "Tank_" + i, new GasStorageSerializer<>(storage));
        }
    }

    @Override
    public void readData(ValueInput view) {
        for (int i = 0; i < this.storages.size(); i++) {
            T storage = this.storages.get(i);
            if (storage == null)
                continue;

            ViewUtils.readChild(view, "Tank_" + i, new GasStorageSerializer<>(storage));
        }
    }

    public record GasStorageSerializer<T extends Storage<GasVariant>>(T storage) implements ViewSerializable {
        @Override
        public void writeData(ValueOutput view) {
            if (storage instanceof SingleGasStorage singleGasStorage) {
                view.putLong("Amount", singleGasStorage.getAmount());
                view.store("Gas", GasVariant.CODEC, singleGasStorage.getResource());
            } else {
                throw new UnsupportedOperationException("Cannot write gas storage of type: " + storage.getClass().getName());
            }
        }

        @Override
        public void readData(ValueInput view) {
            if (storage instanceof SingleGasStorage singleGasStorage) {
                singleGasStorage.amount = view.getLongOr("Amount", 0L);
                singleGasStorage.variant = view.read("Gas", GasVariant.CODEC)
                        .orElse(GasVariant.blank());
            } else {
                throw new UnsupportedOperationException("Cannot read gas storage of type: " + storage.getClass().getName());
            }
        }
    }
}