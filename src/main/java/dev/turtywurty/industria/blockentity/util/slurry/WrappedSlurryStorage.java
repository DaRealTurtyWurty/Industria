package dev.turtywurty.industria.blockentity.util.slurry;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.fabricslurryapi.api.storage.SingleSlurryStorage;
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
    public void writeData(ValueOutput view) {
        for (int i = 0; i < this.storages.size(); i++) {
            T storage = this.storages.get(i);
            if (storage == null)
                continue;

            ViewUtils.putChild(view, "SlurryTank_" + i, new SlurryStorageSerializer<>(storage));
        }
    }

    @Override
    public void readData(ValueInput view) {
        for (int i = 0; i < this.storages.size(); i++) {
            T storage = this.storages.get(i);
            if (storage == null)
                continue;

            ViewUtils.readChild(view, "SlurryTank_" + i, new SlurryStorageSerializer<>(storage));
        }
    }

    public record SlurryStorageSerializer<T extends Storage<SlurryVariant>>(T storage) implements ViewSerializable {
        @Override
        public void writeData(ValueOutput view) {
            if (storage instanceof SingleSlurryStorage singleSlurryStorage) {
                view.putLong("Amount", singleSlurryStorage.getAmount());
                view.store("Slurry", SlurryVariant.CODEC, singleSlurryStorage.getResource());
            } else {
                throw new UnsupportedOperationException("Cannot write slurry storage of type: " + storage.getClass().getName());
            }
        }

        @Override
        public void readData(ValueInput view) {
            if (storage instanceof SingleSlurryStorage singleSlurryStorage) {
                singleSlurryStorage.amount = view.getLongOr("Amount", 0L);
                singleSlurryStorage.variant = view.read("Slurry", SlurryVariant.CODEC).orElse(SlurryVariant.blank());
            } else {
                throw new UnsupportedOperationException("Cannot read slurry storage of type: " + storage.getClass().getName());
            }
        }
    }
}
