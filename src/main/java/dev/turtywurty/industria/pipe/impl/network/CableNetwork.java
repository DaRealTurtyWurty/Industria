package dev.turtywurty.industria.pipe.impl.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.init.PipeNetworkTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkType;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ExtraPacketCodecs;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.Set;
import java.util.UUID;

public class CableNetwork extends PipeNetwork<EnergyStorage> {
    public static final MapCodec<CableNetwork> CODEC = PipeNetwork.createCodec(
            Codec.LONG.fieldOf("storageAmount").forGetter(network -> network.storage.getAmount()),
            (storage, storageAmount) -> ((CableNetworkEnergyStorage) storage).amount = storageAmount,
            CableNetwork::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, CableNetwork> STREAM_CODEC =
            PipeNetwork.createPacketCodec(
                    ByteBufCodecs.LONG,
                    network -> ((CableNetworkEnergyStorage) network.storage).amount,
                    (storage, storageAmount) -> ((CableNetworkEnergyStorage) storage).amount = storageAmount,
                    CableNetwork::new);

    public static final Codec<Set<CableNetwork>> SET_CODEC = ExtraCodecs.setOf(CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Set<CableNetwork>> SET_STREAM_CODEC =
            ExtraPacketCodecs.setOf(STREAM_CODEC);

    public CableNetwork(UUID id) {
        super(id, TransferType.ENERGY);
    }

    @Override
    protected EnergyStorage createStorage() {
        return new CableNetworkEnergyStorage(this);
    }

    @Override
    protected PipeNetworkType<EnergyStorage, CableNetwork> getType() {
        return PipeNetworkTypeInit.ENERGY;
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
