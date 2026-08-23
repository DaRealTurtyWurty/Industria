package dev.turtywurty.industria.pipe.impl.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.turtywurty.slurryapi.SlurryApi;
import dev.turtywurty.slurryapi.api.Slurry;
import dev.turtywurty.slurryapi.api.SlurryVariant;
import dev.turtywurty.slurryapi.api.storage.SingleSlurryStorage;
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

public class SlurryPipeNetwork extends PipeNetwork<ResourceVariant<Slurry>> {
    public static final MapCodec<SlurryPipeNetwork> CODEC = PipeNetwork.createCodec(
            PipeStorageContent.codec(SlurryVariant.CODEC).fieldOf("storageContent")
                    .forGetter(network -> new PipeStorageContent<>(network.storage.resource(0), network.storage.amount(0))),
            (storage, storageContent) -> PipeStorageUtil.restore(
                    (SingleSlurryStorage) storage, storageContent.variant(), storageContent.amount()),
            SlurryPipeNetwork::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, SlurryPipeNetwork> STREAM_CODEC =
            PipeNetwork.createPacketCodec(
                    PipeStorageContent.streamCodec(SlurryVariant.STREAM_CODEC),
                    network -> new PipeStorageContent<>(network.storage.resource(0), network.storage.amount(0)),
                    (storage, storageContent) -> PipeStorageUtil.restore(
                            (SingleSlurryStorage) storage, storageContent.variant(), storageContent.amount()),
                    SlurryPipeNetwork::new);

    public static final Codec<Set<SlurryPipeNetwork>> SET_CODEC = ExtraCodecs.setOf(CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Set<SlurryPipeNetwork>> SET_STREAM_CODEC =
            ExtraStreamCodecs.setOf(STREAM_CODEC);

    public SlurryPipeNetwork(UUID id) {
        super(id, TransferType.SLURRY);
    }

    @Override
    protected SingleSlurryStorage createStorage() {
        return new SingleSlurryStorage() {
            @Override
            protected long getCapacity(ResourceVariant<Slurry> variant) {
                return PipeStorageUtil.scaledCapacity(
                        SlurryPipeNetwork.this.pipes.size(), SlurryApi.BUCKET.numerator());
            }
        };
    }

    @Override
    protected PipeNetworkType<ResourceVariant<Slurry>, SlurryPipeNetwork> getType() {
        return ModPipeNetworkTypes.SLURRY.get();
    }

    public ResourceVariant<Slurry> getSlurryVariant() {
        return this.storage.resource(0);
    }

    public long getSlurryAmount() {
        return this.storage.amount(0);
    }
}
