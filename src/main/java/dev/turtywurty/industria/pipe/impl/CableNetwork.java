package dev.turtywurty.industria.pipe.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.util.Uuids;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.UUID;

public class CableNetwork extends PipeNetwork<EnergyStorage> {
    public static final MapCodec<CableNetwork> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Uuids.CODEC.fieldOf("id").forGetter(CableNetwork::getId),
                    BLOCK_POS_SET_CODEC.fieldOf("pipes").forGetter(CableNetwork::getPipes),
                    BLOCK_POS_SET_CODEC.fieldOf("connectedBlocks").forGetter(CableNetwork::getConnectedBlocks),
                    TransferType.CODEC.fieldOf("transferType").forGetter(CableNetwork::getTransferType),
                    Codec.LONG.fieldOf("storageAmount").forGetter(network -> network.storage.getAmount())
            ).apply(instance, (id, pipes, connectedBlocks, transferType, storageAmount) -> {
                var network = new CableNetwork(id);
                network.pipes.addAll(pipes);
                network.connectedBlocks.addAll(connectedBlocks);
                ((CableNetworkEnergyStorage) network.storage).amount = storageAmount;

                return network;
            }));

    public CableNetwork(UUID id) {
        super(id, TransferType.ENERGY);
    }

    @Override
    protected EnergyStorage createStorage() {
        return new CableNetworkEnergyStorage(this);
    }

    @Override
    public MapCodec<? extends PipeNetwork<?>> getCodec() {
        return CODEC;
    }

    public static class CableNetworkEnergyStorage extends SimpleEnergyStorage {
        private final CableNetwork network;

        public CableNetworkEnergyStorage(CableNetwork network) {
            super(network.pipes.size() * 10_000L, 10_000L, 10_000L);
            this.network = network;
        }

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
            return network.pipes.size() * 10_000L;
        }
    }
}
