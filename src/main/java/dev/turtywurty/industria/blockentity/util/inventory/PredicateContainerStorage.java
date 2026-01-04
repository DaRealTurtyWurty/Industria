package dev.turtywurty.industria.blockentity.util.inventory;

import com.google.common.collect.MapMaker;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BooleanSupplier;

public record PredicateContainerStorage(ContainerStorage delegate, BooleanSupplier canInsert,
                                        BooleanSupplier canExtract) implements ContainerStorage {
    private static final ConcurrentMap<ContainerStorage, ConcurrentMap<PredicateKey, PredicateContainerStorage>> CACHE =
            new MapMaker().weakKeys().makeMap();

    public static PredicateContainerStorage of(ContainerStorage ContainerStorage, BooleanSupplier canInsert, BooleanSupplier canExtract) {
        ConcurrentMap<PredicateKey, PredicateContainerStorage> inventoryCache =
                CACHE.computeIfAbsent(ContainerStorage, k -> new MapMaker().makeMap());

        PredicateKey key = new PredicateKey(canInsert, canExtract);
        return inventoryCache.computeIfAbsent(key,
                k -> new PredicateContainerStorage(ContainerStorage, canInsert, canExtract));
    }

    @Override
    public @UnmodifiableView List<SingleSlotStorage<ItemVariant>> getSlots() {
        return delegate.getSlots();
    }

    @Override
    public boolean supportsInsertion() {
        return delegate.supportsInsertion() && canInsert.getAsBoolean();
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return delegate.insert(resource, maxAmount, transaction);
    }

    @Override
    public boolean supportsExtraction() {
        return delegate.supportsExtraction() && canExtract.getAsBoolean();
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        return delegate.extract(resource, maxAmount, transaction);
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return delegate.iterator();
    }

    @Override
    public int getSlotCount() {
        return delegate.getSlotCount();
    }

    @Override
    public SingleSlotStorage<ItemVariant> getSlot(int slot) {
        return delegate.getSlot(slot);
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return "PredicateContainerStorage[%s]".formatted(delegate.toString());
    }

    @Override
    public Iterator<StorageView<ItemVariant>> nonEmptyIterator() {
        return delegate.nonEmptyIterator();
    }

    @Override
    public Iterable<StorageView<ItemVariant>> nonEmptyViews() {
        return delegate.nonEmptyViews();
    }

    @Override
    public long getVersion() {
        return delegate.getVersion();
    }

    private static class PredicateKey {
        private final BooleanSupplier canInsert;
        private final BooleanSupplier canExtract;

        public PredicateKey(BooleanSupplier canInsert, BooleanSupplier canExtract) {
            this.canInsert = canInsert;
            this.canExtract = canExtract;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PredicateKey that = (PredicateKey) o;
            return Objects.equals(canInsert, that.canInsert) &&
                    Objects.equals(canExtract, that.canExtract);
        }

        @Override
        public int hashCode() {
            return Objects.hash(canInsert, canExtract);
        }
    }
}