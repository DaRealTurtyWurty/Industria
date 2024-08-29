package dev.turtywurty.industria.blockentity.util.energy;

import dev.turtywurty.industria.blockentity.util.UpdatableBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.ArrayList;
import java.util.List;

public interface EnergySpreader {
    static long simulateInsertion(EnergyStorage storage, long amount, Transaction outer) {
        try (Transaction inner = outer.openNested()) {
            long max = storage.insert(amount, inner);
            inner.abort();
            return max;
        }
    }

    default void spread(World level, BlockPos worldPosition, SimpleEnergyStorage thisStorage) {
        List<EnergyStorage> storages = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            EnergyStorage storage = EnergyStorage.SIDED.find(level, worldPosition.offset(direction), direction.getOpposite());
            if (storage == null || !storage.supportsInsertion() || storage.getAmount() >= storage.getCapacity())
                continue;

            storages.add(storage);
        }

        if (storages.isEmpty())
            return;

        try (Transaction transaction = Transaction.openOuter()) {
            long currentEnergy = thisStorage.getAmount();
            long totalExtractable = thisStorage.extract(Long.MAX_VALUE, transaction);
            long totalInserted = 0;

            for (EnergyStorage storage : storages) {
                long insertable = simulateInsertion(storage, totalExtractable, transaction);
                long inserted = storage.insert(insertable, transaction);
                totalInserted += inserted;
            }

            if (totalInserted < totalExtractable) {
                thisStorage.amount += totalExtractable - totalInserted;
            }

            transaction.commit();

            if (currentEnergy != thisStorage.getAmount()) {
                if (this instanceof UpdatableBlockEntity updatableBlockEntity) {
                    updatableBlockEntity.update();
                } else if (this instanceof BlockEntity blockEntity) {
                    blockEntity.markDirty();
                }
            }
        }
    }
}
