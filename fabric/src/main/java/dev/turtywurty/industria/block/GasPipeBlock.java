package dev.turtywurty.industria.block;

import dev.turtywurty.gasapi.api.GasVariant;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.impl.network.GasPipeNetwork;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

public class GasPipeBlock extends PipeBlock<Storage<GasVariant>, GasPipeNetwork, Long> {
    public GasPipeBlock(Properties settings) {
        super(settings, GAS_PIPE_SHAPE, TransferType.GAS);
    }

    @Override
    public Long getAmount(Storage<GasVariant> storage) {
        long amount = 0;
        for (StorageView<GasVariant> storageView : storage) {
            amount += storageView.getAmount();
        }

        return amount;
    }

    @Override
    public Long getCapacity(Storage<GasVariant> storage) {
        long capacity = 0;
        for (StorageView<GasVariant> storageView : storage) {
            capacity += storageView.getCapacity();
        }

        return capacity;
    }

    @Override
    public String getUnit() {
        return "mB";
    }
}
