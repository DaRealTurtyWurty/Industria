package dev.turtywurty.industria.blockentity.util.fluid;

import dev.turtywurty.industria.blockentity.util.WrappedStorage;
import dev.turtywurty.industria.util.ViewSerializable;
import dev.turtywurty.industria.util.ViewUtils;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceTypes;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariantCodecs;
import dev.turtywurty.turtymultiloader.transfer.serialization.StorageSnapshot;
import dev.turtywurty.turtymultiloader.transfer.storage.CombinedStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorageView;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public class WrappedFluidStorage<T extends ResourceStorage<ResourceVariant<Fluid>>> extends WrappedStorage<T> {
    public CombinedStorage<ResourceVariant<Fluid>> getCombinedStorage() {
        return new CombinedStorage<>(this.storages);
    }

    public List<FluidStack> getFluids() {
        List<FluidStack> fluids = new ArrayList<>();
        for (T tank : this.storages) {
            for (ResourceStorageView<ResourceVariant<Fluid>> storageView : tank) {
                if (storageView.amount() > 0)
                    fluids.add(new FluidStack(storageView.resource(), storageView.amount()));
            }
        }

        return fluids;
    }

    @Override
    public void writeData(ValueOutput view) {
        for (int i = 0; i < this.storages.size(); i++) {
            T storage = this.storages.get(i);
            if (storage == null)
                continue;

            ViewUtils.putChild(view, "FluidTank_" + i, new FluidStorageSerializer<>(storage));
        }
    }

    @Override
    public void readData(ValueInput view) {
        for (int i = 0; i < this.storages.size(); i++) {
            T storage = this.storages.get(i);
            if (storage == null)
                continue;

            ViewUtils.readChild(view, "FluidTank_" + i, new FluidStorageSerializer<>(storage));
        }
    }

    public record FluidStorageSerializer<T extends ResourceStorage<ResourceVariant<Fluid>>>(T storage)
            implements ViewSerializable {
        @Override
        public void writeData(ValueOutput view) {
            requireSingleSlot(storage);
            view.putLong("Amount", storage.amount(0));
            view.store("Fluid", ResourceVariantCodecs.FLUID, storage.resource(0));
        }

        @Override
        public void readData(ValueInput view) {
            requireSingleSlot(storage);
            long amount = view.getLongOr("Amount", 0L);
            ResourceVariant<Fluid> resource = view.read("Fluid", ResourceVariantCodecs.FLUID)
                    .orElse(ResourceTypes.FLUID.empty());
            if (!new StorageSnapshot<>(List.of(new StorageSnapshot.Entry<>(resource, amount))).apply(storage))
                throw new IllegalStateException("Stored fluid does not fit in " + storage.getClass().getName());
        }

        private static void requireSingleSlot(ResourceStorage<?> storage) {
            if (!storage.hasStableIndices() || storage.size() != 1)
                throw new UnsupportedOperationException(
                        "Wrapped fluid persistence requires one stable slot: " + storage.getClass().getName());
        }
    }
}
