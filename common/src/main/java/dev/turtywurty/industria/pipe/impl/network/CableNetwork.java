package dev.turtywurty.industria.pipe.impl.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.init.ModPipeNetworkTypes;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeStorageUtil;
import dev.turtywurty.industria.pipe.PipeNetworkType;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ExtraStreamCodecs;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.resource.UnitResource;
import dev.turtywurty.turtymultiloader.transfer.storage.SimpleEnergyStorage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Set;
import java.util.UUID;

public class CableNetwork extends PipeNetwork<ResourceVariant<UnitResource>> {
    public static final MapCodec<CableNetwork> CODEC = PipeNetwork.createCodec(
            Codec.LONG.fieldOf("storageAmount").forGetter(network -> network.storage.amount(0)),
            (storage, storageAmount) -> PipeStorageUtil.restore(
                    (CableNetworkEnergyStorage) storage, SimpleEnergyStorage.ENERGY, storageAmount),
            CableNetwork::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, CableNetwork> STREAM_CODEC =
            PipeNetwork.createPacketCodec(
                    ByteBufCodecs.LONG,
                    network -> network.storage.amount(0),
                    (storage, storageAmount) -> PipeStorageUtil.restore(
                            (CableNetworkEnergyStorage) storage, SimpleEnergyStorage.ENERGY, storageAmount),
                    CableNetwork::new);

    public static final Codec<Set<CableNetwork>> SET_CODEC = ExtraCodecs.setOf(CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Set<CableNetwork>> SET_STREAM_CODEC =
            ExtraStreamCodecs.setOf(STREAM_CODEC);

    public CableNetwork(UUID id) {
        super(id, TransferType.ENERGY);
    }

    @Override
    protected CableNetworkEnergyStorage createStorage() {
        return new CableNetworkEnergyStorage(this);
    }

    @Override
    protected PipeNetworkType<ResourceVariant<UnitResource>, CableNetwork> getType() {
        return ModPipeNetworkTypes.ENERGY.get();
    }

    public static class CableNetworkEnergyStorage extends SimpleEnergyStorage {
        private final CableNetwork network;

        public CableNetworkEnergyStorage(CableNetwork network) {
            super();
            this.network = network;
        }

        @Override
        public long getMaxInput() {
            return 10_000L;
        }

        @Override
        public long getMaxOutput() {
            return 10_000L;
        }

        @Override
        public long getCapacity() {
            return PipeStorageUtil.scaledCapacity(this.network.pipes.size(), 10_000L);
        }
    }
}
