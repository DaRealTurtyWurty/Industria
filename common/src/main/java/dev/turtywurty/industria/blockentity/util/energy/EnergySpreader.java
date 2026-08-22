package dev.turtywurty.industria.blockentity.util.energy;

import dev.turtywurty.industria.blockentity.util.UpdateableBlockEntityLike;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import dev.turtywurty.turtymultiloader.transfer.storage.SimpleEnergyStorage;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferContext;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransaction;
import dev.turtywurty.turtymultiloader.transfer.transaction.TransferTransactionScope;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

public interface EnergySpreader {
    static long simulateInsertion(ResourceStorage<ResourceVariant<UnitResource>> storage,
                                  long amount, TransferContext outer) {
        try (TransferTransactionScope inner = outer.openNested()) {
            return storage.insert(SimpleEnergyStorage.ENERGY, amount, inner);
        }
    }

    default void spread(Level world, BlockPos pos, SimpleEnergyStorage energyStorage) {
        List<ResourceStorage<ResourceVariant<UnitResource>>> storages = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            ResourceStorage<ResourceVariant<UnitResource>> storage = TransferType.ENERGY.lookup(
                    world, pos.relative(direction), direction.getOpposite());
            if (storage == null || !storage.supportsInsertion())
                continue;

            storages.add(storage);
        }

        if (storages.isEmpty())
            return;

        try (TransferTransaction transaction = TransferTransaction.openRoot()) {
            long currentEnergy = energyStorage.getAmount();
            long totalExtractable = energyStorage.extract(Long.MAX_VALUE, transaction);
            long totalInserted = 0;

            for (ResourceStorage<ResourceVariant<UnitResource>> storage : storages) {
                long remaining = totalExtractable - totalInserted;
                if (remaining <= 0)
                    break;
                long insertable = simulateInsertion(storage, remaining, transaction);
                long inserted = storage.insert(SimpleEnergyStorage.ENERGY, insertable, transaction);
                totalInserted += inserted;
            }

            if (totalInserted < totalExtractable)
                energyStorage.insertInternal(totalExtractable - totalInserted, transaction);

            transaction.commit();

            if (currentEnergy != energyStorage.getAmount()) {
                if (this instanceof UpdateableBlockEntityLike updatableBlockEntity) {
                    updatableBlockEntity.update();
                } else if (this instanceof BlockEntity blockEntity) {
                    blockEntity.setChanged();
                }
            }
        }
    }
}
