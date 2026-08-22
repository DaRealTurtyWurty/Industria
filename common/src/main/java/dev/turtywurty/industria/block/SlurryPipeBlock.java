package dev.turtywurty.industria.block;

import dev.turtywurty.slurryapi.api.Slurry;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.impl.network.SlurryPipeNetwork;
import dev.turtywurty.industria.pipe.PipeStorageUtil;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;

public class SlurryPipeBlock extends PipeBlock<ResourceVariant<Slurry>, SlurryPipeNetwork> {
    public SlurryPipeBlock(Properties settings) {
        super(settings, SLURRY_PIPE_SHAPE, TransferType.SLURRY);
    }

    @Override
    public long getAmount(ResourceStorage<ResourceVariant<Slurry>> storage) {
        return PipeStorageUtil.amount(storage);
    }

    @Override
    public long getCapacity(ResourceStorage<ResourceVariant<Slurry>> storage) {
        return PipeStorageUtil.capacity(storage);
    }

    @Override
    public String getUnit() {
        return "mB";
    }
}
