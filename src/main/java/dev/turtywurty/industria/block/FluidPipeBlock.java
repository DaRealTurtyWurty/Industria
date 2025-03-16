package dev.turtywurty.industria.block;

import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

public class FluidPipeBlock extends PipeBlock<Storage<FluidVariant>> {
    public FluidPipeBlock(Settings settings) {
        super(settings, 6, TransferType.FLUID, PipeNetworkManager.FLUID);
    }

    @Override
    protected long getAmount(Storage<FluidVariant> storage) {
        long amount = 0;
        for (StorageView<FluidVariant> storageView : storage) {
            amount += storageView.getAmount();
        }

        return amount;
    }

    @Override
    protected long getCapacity(Storage<FluidVariant> storage) {
        long capacity = 0;
        for (StorageView<FluidVariant> storageView : storage) {
            capacity += storageView.getCapacity();
        }

        return capacity;
    }

    @Override
    protected String getUnit() {
        return "mB";
    }
}
