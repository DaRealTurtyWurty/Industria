package dev.turtywurty.industria.blockentity.util.heat;

import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import dev.turtywurty.industria.util.ViewSerializable;
import dev.turtywurty.industria.util.ViewUtils;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class WrappedHeatStorage<T extends HeatStorage> extends WrappedStorage<T> {
    @Override
    public void writeData(ValueOutput view) {
        for (int i = 0; i < this.storages.size(); i++) {
            T storage = this.storages.get(i);
            if (storage == null)
                continue;

            ViewUtils.putChild(view, "HeatStorage_" + i, new HeatStorageSerializer<>(storage));
        }
    }

    @Override
    public void readData(ValueInput view) {
        for (int i = 0; i < this.storages.size(); i++) {
            T storage = this.storages.get(i);
            if (storage == null)
                continue;

            ViewUtils.readChild(view, "HeatStorage_" + i, new HeatStorageSerializer<>(storage));
        }
    }

    public record HeatStorageSerializer<T extends HeatStorage>(T storage) implements ViewSerializable {
        @Override
        public void writeData(ValueOutput view) {
            view.putDouble("Amount", storage.getAmount());
        }

        @Override
        public void readData(ValueInput view) {
            double amount = view.getDoubleOr("Amount", 0);
            if (storage instanceof SimpleHeatStorage simpleHeatStorage) {
                simpleHeatStorage.setAmount(amount);
            } else {
                throw new UnsupportedOperationException("Cannot set amount for storage of type " + storage.getClass().getName());
            }
        }
    }
}