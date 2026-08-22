package dev.turtywurty.industria.pipe.impl.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.turtywurty.gasapi.GasApi;
import dev.turtywurty.gasapi.api.Gas;
import dev.turtywurty.gasapi.api.GasVariant;
import dev.turtywurty.gasapi.api.storage.SingleGasStorage;
import dev.turtywurty.industria.init.ModPipeNetworkTypes;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkType;
import dev.turtywurty.industria.pipe.PipeStorageContent;
import dev.turtywurty.industria.pipe.PipeStorageUtil;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ExtraStreamCodecs;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Set;
import java.util.UUID;

public class GasPipeNetwork extends PipeNetwork<ResourceVariant<Gas>> {
    public static final MapCodec<GasPipeNetwork> CODEC = PipeNetwork.createCodec(
            PipeStorageContent.codec(GasVariant.CODEC).fieldOf("storageContent")
                    .forGetter(network -> new PipeStorageContent<>(network.storage.resource(0), network.storage.amount(0))),
            (storage, storageContent) -> PipeStorageUtil.restore(
                    (SingleGasStorage) storage, storageContent.variant(), storageContent.amount()),
            GasPipeNetwork::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, GasPipeNetwork> STREAM_CODEC =
            PipeNetwork.createPacketCodec(
                    PipeStorageContent.streamCodec(GasVariant.STREAM_CODEC),
                    network -> new PipeStorageContent<>(network.storage.resource(0), network.storage.amount(0)),
                    (storage, storageContent) -> PipeStorageUtil.restore(
                            (SingleGasStorage) storage, storageContent.variant(), storageContent.amount()),
                    GasPipeNetwork::new);

    public static final Codec<Set<GasPipeNetwork>> SET_CODEC = ExtraCodecs.setOf(CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Set<GasPipeNetwork>> SET_STREAM_CODEC =
            ExtraStreamCodecs.setOf(STREAM_CODEC);

    public GasPipeNetwork(UUID id) {
        super(id, TransferType.GAS);
    }

    @Override
    protected SingleGasStorage createStorage() {
        return new SingleGasStorage() {
            @Override
            protected long getCapacity(ResourceVariant<Gas> variant) {
                return PipeStorageUtil.scaledCapacity(
                        GasPipeNetwork.this.pipes.size(), GasApi.BUCKET.numerator());
            }
        };
    }

    @Override
    protected PipeNetworkType<ResourceVariant<Gas>, GasPipeNetwork> getType() {
        return ModPipeNetworkTypes.GAS.get();
    }
}
