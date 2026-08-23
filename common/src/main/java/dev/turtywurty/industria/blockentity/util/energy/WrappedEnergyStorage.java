package dev.turtywurty.industria.blockentity.util.energy;

import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import dev.turtywurty.industria.util.ViewSerializable;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import dev.turtywurty.turtymultiloader.transfer.serialization.StorageSnapshot;
import dev.turtywurty.turtymultiloader.transfer.storage.CombinedStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.SimpleEnergyStorage;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

public class WrappedEnergyStorage extends WrappedStorage<ResourceStorage<ResourceVariant<UnitResource>>> {
    public CombinedStorage<ResourceVariant<UnitResource>> getCombinedStorage() {
        return new CombinedStorage<>(this.storages);
    }

    @Override
    public void writeData(ValueOutput view) {
        for (int i = 0; i < this.storages.size(); i++) {
            ResourceStorage<ResourceVariant<UnitResource>> storage = this.storages.get(i);
            ViewUtils.putChild(view, "EnergyStorage_" + i, new EnergyStorageSerializer(storage));
        }
    }

    @Override
    public void readData(ValueInput view) {
        for (int i = 0; i < this.storages.size(); i++) {
            ResourceStorage<ResourceVariant<UnitResource>> storage = this.storages.get(i);
            ViewUtils.readChild(view, "EnergyStorage_" + i, new EnergyStorageSerializer(storage));
        }
    }

    public record EnergyStorageSerializer(ResourceStorage<ResourceVariant<UnitResource>> storage)
            implements ViewSerializable {
        @Override
        public void writeData(ValueOutput view) {
            requireSingleSlot(storage);
            view.putLong("Amount", storage.amount(0));
        }

        @Override
        public void readData(ValueInput view) {
            requireSingleSlot(storage);
            long amount = view.getLongOr("Amount", 0L);
            if (!new StorageSnapshot<>(List.of(
                    new StorageSnapshot.Entry<>(SimpleEnergyStorage.ENERGY, amount))).apply(storage))
                throw new IllegalStateException("Stored energy does not fit in " + storage.getClass().getName());
        }

        private static void requireSingleSlot(ResourceStorage<?> storage) {
            if (!storage.hasStableIndices() || storage.size() != 1)
                throw new UnsupportedOperationException(
                        "Wrapped energy persistence requires one stable slot: " + storage.getClass().getName());
        }
    }
}
