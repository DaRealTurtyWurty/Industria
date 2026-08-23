package dev.turtywurty.industria.blockentity.util.inventory;

import com.google.common.collect.MapMaker;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ContainerStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.TransferSupport;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferContext;
import net.minecraft.world.item.Item;

import java.util.concurrent.ConcurrentMap;
import java.util.function.BooleanSupplier;

public record PredicateContainerStorage(
        ContainerStorage delegate,
        BooleanSupplier canInsert,
        BooleanSupplier canExtract
) implements ResourceStorage<ResourceVariant<Item>> {
    private static final ConcurrentMap<ContainerStorage, ConcurrentMap<PredicateKey, PredicateContainerStorage>> CACHE =
            new MapMaker().weakKeys().makeMap();

    public static PredicateContainerStorage of(
            ContainerStorage containerStorage,
            BooleanSupplier canInsert,
            BooleanSupplier canExtract) {
        ConcurrentMap<PredicateKey, PredicateContainerStorage> inventoryCache =
                CACHE.computeIfAbsent(containerStorage, _ -> new MapMaker().makeMap());

        var key = new PredicateKey(canInsert, canExtract);
        return inventoryCache.computeIfAbsent(key,
                _ -> new PredicateContainerStorage(containerStorage, canInsert, canExtract));
    }

    @Override
    public boolean hasStableIndices() {
        return this.delegate.hasStableIndices();
    }

    @Override
    public int size() {
        return this.delegate.size();
    }

    @Override
    public ResourceVariant<Item> resource(int index) {
        return this.delegate.resource(index);
    }

    @Override
    public long amount(int index) {
        return this.delegate.amount(index);
    }

    @Override
    public long capacity(int index, ResourceVariant<Item> resource) {
        return this.delegate.capacity(index, resource);
    }

    @Override
    public boolean isValid(int index, ResourceVariant<Item> resource) {
        return this.delegate.isValid(index, resource);
    }

    @Override
    public TransferSupport support(int index) {
        TransferSupport restriction = this.canInsert.getAsBoolean()
                ? this.canExtract.getAsBoolean() ? TransferSupport.BOTH : TransferSupport.INSERT_ONLY
                : this.canExtract.getAsBoolean() ? TransferSupport.EXTRACT_ONLY : TransferSupport.NONE;
        return this.delegate.support(index).and(restriction);
    }

    @Override
    public long insert(
            int index,
            ResourceVariant<Item> resource,
            long maxAmount,
            TransferContext transaction) {
        return this.canInsert.getAsBoolean()
                ? this.delegate.insert(index, resource, maxAmount, transaction)
                : 0;
    }

    @Override
    public long extract(
            int index,
            ResourceVariant<Item> resource,
            long maxAmount,
            TransferContext transaction) {
        return this.canExtract.getAsBoolean()
                ? this.delegate.extract(index, resource, maxAmount, transaction)
                : 0;
    }

    @Override
    public boolean supportsInsertion() {
        return this.canInsert.getAsBoolean() && this.delegate.supportsInsertion();
    }

    @Override
    public boolean supportsExtraction() {
        return this.canExtract.getAsBoolean() && this.delegate.supportsExtraction();
    }

    @Override
    public String toString() {
        return "PredicateContainerStorage[%s]".formatted(this.delegate);
    }

    @Override
    public long version() {
        return this.delegate.version();
    }

    private record PredicateKey(BooleanSupplier canInsert, BooleanSupplier canExtract) {
    }
}
