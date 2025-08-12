package dev.turtywurty.industria.blockentity.util.fluid;

import dev.turtywurty.industria.blockentity.util.WrappedStorage;
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
        for (T tank : this.storages) {
            if (tank instanceof SingleFluidStorage singleFluidStorage) {
                view.putLong("Amount", singleFluidStorage.getAmount());
                view.put("Fluid", FluidVariant.CODEC, singleFluidStorage.getResource());
            }
        }
    }

    @Override
    public void readData(ReadView view) {
        for (T storage : this.storages) {
            if (storage == null)
                continue;

            if (storage instanceof SingleFluidStorage singleFluidStorage) {
                singleFluidStorage.amount = view.getLong("Amount", 0L);
                singleFluidStorage.variant = view.read("Fluid", FluidVariant.CODEC).orElseThrow();
            } else {
                throw new UnsupportedOperationException("Cannot read fluid storage of type: " + storage.getClass().getName());
            }
        }
    }
}
