package dev.turtywurty.industria.util;

import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorageView;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class TransferUtils {
    private TransferUtils() {
    }

    public static <V extends ResourceVariant<?>> Optional<V> findFirstVariant(
            ResourceStorage<V> storage, @Nullable V checkFirst) {
        if (storage.hasStableIndices() && storage.size() == 1)
            return Optional.of(storage.resource(0));

        if (checkFirst != null && !checkFirst.isBlank()) {
            try (TransferTransaction transaction = TransferTransaction.openRoot()) {
                return storage.extract(checkFirst, FluidAmounts.BUCKET, transaction) > 0
                        ? Optional.of(checkFirst)
                        : Optional.empty();
            }
        }

        if (storage.hasStableIndices()) {
            for (ResourceStorageView<V> view : storage) {
                if (view.amount() > 0)
                    return Optional.of(view.resource());
            }
        }

        return Optional.empty();
    }
}
