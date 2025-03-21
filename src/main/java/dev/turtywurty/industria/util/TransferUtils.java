package dev.turtywurty.industria.util;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TransferUtils {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <V, T extends TransferVariant<V>> Optional<T> findFirstVariant(Storage<T> storage, @Nullable T checkFirst) {
        if (storage instanceof SingleVariantStorage singleFluidStorage) {
            return Optional.ofNullable((T) singleFluidStorage.variant);
        }

        if (checkFirst != null && !checkFirst.isBlank()) {
            try (Transaction transaction = Transaction.openOuter()) {
                if (storage.extract(checkFirst, FluidConstants.BUCKET, transaction) > 0) {
                    return Optional.of(checkFirst);
                }

                return Optional.empty();
            }
        }

        for (StorageView<T> storageView : storage.nonEmptyViews()) {
            return Optional.ofNullable(storageView.getResource());
        }

        return Optional.empty();
    }
}
