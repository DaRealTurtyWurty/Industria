package dev.turtywurty.industria.block;

import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.impl.SlurryPipeNetwork;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;

public class SlurryPipeBlock extends PipeBlock<Storage<SlurryVariant>, SlurryPipeNetwork, Long> {
    public SlurryPipeBlock(Settings settings) {
        super(settings, 6, TransferType.SLURRY);
    }

    @Override
    public Long getAmount(Storage<SlurryVariant> storage) {
        long amount = 0;
        for (StorageView<SlurryVariant> storageView : storage) {
            amount += storageView.getAmount();
        }

        return amount;
    }

    @Override
    public Long getCapacity(Storage<SlurryVariant> storage) {
        long capacity = 0;
        for (StorageView<SlurryVariant> storageView : storage) {
            capacity += storageView.getCapacity();
        }

        return capacity;
    }

    @Override
    public String getUnit() {
        return "mB";
    }
}
