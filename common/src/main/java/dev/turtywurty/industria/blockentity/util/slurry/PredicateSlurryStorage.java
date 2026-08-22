package dev.turtywurty.industria.blockentity.util.slurry;

import dev.turtywurty.slurryapi.api.Slurry;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferContext;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.function.Predicate;

public class PredicateSlurryStorage extends SyncingSlurryStorage {
    private final Predicate<ResourceVariant<Slurry>> canInsert;
    private final Predicate<ResourceVariant<Slurry>> canExtract;

    public PredicateSlurryStorage(BlockEntity blockEntity, long capacity,
                                  Predicate<ResourceVariant<Slurry>> canInsert,
                                  Predicate<ResourceVariant<Slurry>> canExtract) {
        super(blockEntity, capacity);
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @Override
    public long insert(int index, ResourceVariant<Slurry> resource, long maxAmount, TransferContext transaction) {
        return this.canInsert.test(resource) ? super.insert(index, resource, maxAmount, transaction) : 0;
    }

    @Override
    public long extract(int index, ResourceVariant<Slurry> resource, long maxAmount, TransferContext transaction) {
        return this.canExtract.test(resource) ? super.extract(index, resource, maxAmount, transaction) : 0;
    }
}
