package dev.turtywurty.industria.blockentity.util.fluid;

import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;

import java.util.function.Predicate;

public class PredicateFluidStorage extends SyncingFluidStorage {
    private final Predicate<ResourceVariant<Fluid>> canInsert;
    private final Predicate<ResourceVariant<Fluid>> canExtract;

    public PredicateFluidStorage(BlockEntity blockEntity, long capacity,
                                 Predicate<ResourceVariant<Fluid>> canInsert,
                                 Predicate<ResourceVariant<Fluid>> canExtract) {
        super(blockEntity, capacity);
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @Override
    public long insert(int index, ResourceVariant<Fluid> resource, long maxAmount, TransferContext transaction) {
        return this.canInsert.test(resource) ? super.insert(index, resource, maxAmount, transaction) : 0;
    }

    @Override
    public long extract(int index, ResourceVariant<Fluid> resource, long maxAmount, TransferContext transaction) {
        return this.canExtract.test(resource) ? super.extract(index, resource, maxAmount, transaction) : 0;
    }
}
