package dev.turtywurty.industria.pipe;

import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.MutableResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;

public final class PipeStorageUtil {
    private PipeStorageUtil() {
    }

    public static <V extends ResourceVariant<?>> long amount(ResourceStorage<V> storage) {
        requireIndexed(storage);
        long amount = 0;
        for (int index = 0; index < storage.size(); index++)
            amount = saturatedAdd(amount, storage.amount(index));
        return amount;
    }

    public static <V extends ResourceVariant<?>> long capacity(ResourceStorage<V> storage) {
        requireIndexed(storage);
        long capacity = 0;
        for (int index = 0; index < storage.size(); index++)
            capacity = saturatedAdd(capacity, storage.capacity(index, storage.resource(index)));
        return capacity;
    }

    public static <V extends ResourceVariant<?>> void restore(
            MutableResourceStorage<V> storage,
            V resource,
            long amount) {
        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            if (!storage.set(0, resource, amount, transaction))
                throw new IllegalArgumentException("Stored pipe contents exceed the pipe network capacity");
            transaction.commit();
        }
    }

    public static long scaledCapacity(int pipeCount, long capacityPerPipe) {
        if (pipeCount <= 0 || capacityPerPipe <= 0)
            return 0;
        try {
            return Math.multiplyExact((long) pipeCount, capacityPerPipe);
        } catch (ArithmeticException ignored) {
            return Long.MAX_VALUE;
        }
    }

    private static void requireIndexed(ResourceStorage<?> storage) {
        if (!storage.hasStableIndices())
            throw new IllegalArgumentException("Pipe storage must expose stable indices");
    }

    private static long saturatedAdd(long left, long right) {
        if (right > Long.MAX_VALUE - left)
            return Long.MAX_VALUE;
        return left + right;
    }
}
