package dev.turtywurty.industria.blockentity.util.fluid;

import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import dev.turtywurty.industria.util.ViewSerializable;
import dev.turtywurty.industria.util.ViewUtils;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

import java.util.ArrayList;
import java.util.List;

public class WrappedFluidStorage<T extends Storage<FluidVariant>> extends WrappedStorage<T> {
    private final CombinedStorage<FluidVariant, T> combinedStorage = new CombinedStorage<>(this.storages);

    public CombinedStorage<FluidVariant, T> getCombinedStorage() {
        return this.combinedStorage;
    }

    public List<FluidStack> getFluids() {
        List<FluidStack> fluids = new ArrayList<>();
        for (T tank : this.storages) {
            for (StorageView<FluidVariant> view : tank.nonEmptyViews()) {
                fluids.add(new FluidStack(view.getResource(), view.getAmount()));
            }
        }

        return fluids;
    }

    @Override
    public void writeData(WriteView view) {
        for (int i = 0; i < this.storages.size(); i++) {
            T storage = this.storages.get(i);
            if (storage == null)
                continue;

            ViewUtils.putChild(view, "FluidTank_" + i, new FluidStorageSerializer<>(storage));
        }
    }

    @Override
    public void readData(ReadView view) {
        for (int i = 0; i < this.storages.size(); i++) {
            T storage = this.storages.get(i);
            if (storage == null)
                continue;

            ViewUtils.readChild(view, "FluidTank_" + i, new FluidStorageSerializer<>(storage));
        }
    }

    public record FluidStorageSerializer<T extends Storage<FluidVariant>>(T storage) implements ViewSerializable {
        @Override
        public void writeData(WriteView view) {
            if (storage instanceof SingleFluidStorage singleFluidStorage) {
                view.putLong("Amount", singleFluidStorage.getAmount());
                view.put("Fluid", FluidVariant.CODEC, singleFluidStorage.variant);
            } else {
                throw new UnsupportedOperationException("Cannot write fluid storage of type: " + storage.getClass().getName());
            }
        }

        @Override
        public void readData(ReadView view) {
            if (storage instanceof SingleFluidStorage singleFluidStorage) {
                singleFluidStorage.amount = view.getLong("Amount", 0L);
                singleFluidStorage.variant = view.read("Fluid", FluidVariant.CODEC).orElse(FluidVariant.blank());
            } else {
                throw new UnsupportedOperationException("Cannot read fluid storage of type: " + storage.getClass().getName());
            }
        }
    }
}
