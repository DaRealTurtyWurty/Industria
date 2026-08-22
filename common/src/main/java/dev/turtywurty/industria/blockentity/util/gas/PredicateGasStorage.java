package dev.turtywurty.industria.blockentity.util.gas;

import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferContext;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Predicate;

public class PredicateGasStorage extends SyncingGasStorage {
    private final Predicate<ResourceVariant<Gas>> canInsert;
    private final Predicate<ResourceVariant<Gas>> canExtract;

    public PredicateGasStorage(BlockEntity blockEntity, long capacity,
                               Predicate<ResourceVariant<Gas>> canInsert,
                               Predicate<ResourceVariant<Gas>> canExtract) {
        super(blockEntity, capacity);
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @Override
    public long insert(int index, ResourceVariant<Gas> resource, long maxAmount, TransferContext transaction) {
        return this.canInsert.test(resource) ? super.insert(index, resource, maxAmount, transaction) : 0;
    }

    @Override
    public long extract(int index, ResourceVariant<Gas> resource, long maxAmount, TransferContext transaction) {
        return this.canExtract.test(resource) ? super.extract(index, resource, maxAmount, transaction) : 0;
    }
}
