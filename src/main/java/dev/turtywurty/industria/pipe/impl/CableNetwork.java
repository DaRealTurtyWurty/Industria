package dev.turtywurty.industria.pipe.impl;

import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.UUID;

public class CableNetwork extends PipeNetwork<EnergyStorage> {
    public CableNetwork(UUID id) {
        super(id, TransferType.ENERGY);
    }

    @Override
    protected EnergyStorage createStorage() {
        return new SimpleEnergyStorage(this.pipes.size() * 10_000L, 10_000L, 10_000L) {
            @Override
            public long insert(long maxAmount, TransactionContext transaction) {
                StoragePreconditions.notNegative(maxAmount);

                long inserted = Math.min(maxInsert, Math.min(maxAmount, getCapacity() - amount));

                if (inserted > 0) {
                    updateSnapshots(transaction);
                    amount += inserted;
                    return inserted;
                }

                return 0;
            }

            @Override
            public long getCapacity() {
                return CableNetwork.this.pipes.size() * 10_000L;
            }
        };
    }

    @Override
    public NbtCompound writeNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = new NbtCompound();
        nbt.put("networkData", super.writeNbt(registryLookup));
        nbt.putLong("amount", this.storage.getAmount());
        return nbt;
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt.getCompound("networkData"), registryLookup);
        ((SimpleEnergyStorage) this.storage).amount = nbt.getLong("amount");
    }
}
