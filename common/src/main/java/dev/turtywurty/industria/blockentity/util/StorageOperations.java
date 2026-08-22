package dev.turtywurty.industria.blockentity.util;

import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.MutableResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.SimpleEnergyStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;

/**
 * Transactional helpers for mutations performed by a storage's owning machine.
 */
public final class StorageOperations {
    private StorageOperations() {
    }

    public static <V extends ResourceVariant<?>> boolean set(
            MutableResourceStorage<V> storage, V resource, long amount) {
        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            if (!storage.set(0, resource, amount, transaction))
                return false;

            transaction.commit();
            return true;
        }
    }

    public static <V extends ResourceVariant<?>> long insert(
            MutableResourceStorage<V> storage, V resource, long amount) {
        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            long inserted = storage.insertInternal(resource, amount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    public static <V extends ResourceVariant<?>> long extract(
            MutableResourceStorage<V> storage, V resource, long amount) {
        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            long extracted = storage.extractInternal(resource, amount, transaction);
            transaction.commit();
            return extracted;
        }
    }

    public static boolean setEnergy(SimpleEnergyStorage storage, long amount) {
        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            if (!storage.setAmount(amount, transaction))
                return false;

            transaction.commit();
            return true;
        }
    }

    public static long insertEnergy(SimpleEnergyStorage storage, long amount) {
        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            long inserted = storage.insertInternal(amount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    public static long extract(SimpleEnergyStorage storage, long amount) {
        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            long extracted = storage.extractInternal(amount, transaction);
            transaction.commit();
            return extracted;
        }
    }
}
