package dev.turtywurty.industria.block;

import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.impl.network.CableNetwork;
import dev.turtywurty.industria.pipe.PipeStorageUtil;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;

public class CableBlock extends PipeBlock<ResourceVariant<UnitResource>, CableNetwork> {
    public CableBlock(Properties settings) {
        super(settings, CABLE_SHAPE, TransferType.ENERGY);
    }

    @Override
    public long getAmount(ResourceStorage<ResourceVariant<UnitResource>> storage) {
        return PipeStorageUtil.amount(storage);
    }

    @Override
    public long getCapacity(ResourceStorage<ResourceVariant<UnitResource>> storage) {
        return PipeStorageUtil.capacity(storage);
    }

    @Override
    public String getUnit() {
        return "FE";
    }
}
