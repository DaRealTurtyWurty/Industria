package dev.turtywurty.industria.block;

import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.impl.network.GasPipeNetwork;
import dev.turtywurty.industria.pipe.PipeStorageUtil;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;

public class GasPipeBlock extends PipeBlock<ResourceVariant<Gas>, GasPipeNetwork> {
    public GasPipeBlock(Properties settings) {
        super(settings, GAS_PIPE_SHAPE, TransferType.GAS);
    }

    @Override
    public long getAmount(ResourceStorage<ResourceVariant<Gas>> storage) {
        return PipeStorageUtil.amount(storage);
    }

    @Override
    public long getCapacity(ResourceStorage<ResourceVariant<Gas>> storage) {
        return PipeStorageUtil.capacity(storage);
    }

    @Override
    public String getUnit() {
        return "mB";
    }
}
