package dev.turtywurty.industria.blockentity.util.slurry;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.fabricslurryapi.api.storage.SingleSlurryStorage;
import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

import java.util.ArrayList;
import java.util.List;

public class WrappedSlurryStorage<T extends Storage<SlurryVariant>> extends WrappedStorage<T> {
    private final CombinedStorage<SlurryVariant, T> combinedStorage = new CombinedStorage<>(this.storages);

    public CombinedStorage<SlurryVariant, T> getCombinedStorage() {
        return this.combinedStorage;
    }

    public List<SlurryStack> getSlurries() {
        List<SlurryStack> slurries = new ArrayList<>();
        for (T tank : this.storages) {
            for (StorageView<SlurryVariant> view : tank.nonEmptyViews()) {
                slurries.add(new SlurryStack(view.getResource(), view.getAmount()));
            }
        }

        return slurries;
    }

    @Override
    public void writeData(WriteView view) {
        for (T tank : this.storages) {
            if (tank instanceof SingleSlurryStorage singleSlurryStorage) {
                view.putLong("Amount", singleSlurryStorage.getAmount());
                view.put("Slurry", SlurryVariant.CODEC, singleSlurryStorage.getResource());
            }
        }
    }

    @Override
    public void readData(ReadView view) {
        for (T storage : this.storages) {
            if (storage == null)
                continue;

            if (storage instanceof SingleSlurryStorage singleSlurryStorage) {
                singleSlurryStorage.amount = view.getLong("Amount", 0L);
                singleSlurryStorage.variant = view.read("Slurry", SlurryVariant.CODEC)
                        .orElse(SlurryVariant.blank());
            } else {
                throw new UnsupportedOperationException("Cannot read slurry storage of type: " + storage.getClass().getName());
            }
        }
    }
}
