package dev.turtywurty.industria.blockentity.util.heat;

import dev.turtywurty.heatapi.api.HeatStorage;
import dev.turtywurty.heatapi.api.base.SimpleHeatStorage;
import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

public class WrappedHeatStorage<T extends HeatStorage> extends WrappedStorage<T> {
    @Override
    public void writeData(WriteView view) {
        for (HeatStorage storage : this.storages) {
            view.putDouble("Amount", storage.getAmount());
        }
    }

    @Override
    public void readData(ReadView view) {
        for (HeatStorage storage : this.storages) {
            double amount = view.getDouble("Amount", 0);
            if (storage instanceof SimpleHeatStorage simpleHeatStorage) {
                simpleHeatStorage.setAmount(amount);
            } else {
                throw new UnsupportedOperationException("Cannot set amount for storage of type " + storage.getClass().getName());
            }
        }
    }
}