package dev.turtywurty.industria.pipe.impl.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.fabricslurryapi.api.storage.SingleSlurryStorage;
import dev.turtywurty.industria.blockentity.util.slurry.SlurryStack;
import dev.turtywurty.industria.init.PipeNetworkTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkType;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ExtraPacketCodecs;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.Set;
import java.util.UUID;

public class SlurryPipeNetwork extends PipeNetwork<Storage<SlurryVariant>> {
    public static final MapCodec<SlurryPipeNetwork> CODEC = PipeNetwork.createCodec(
            SlurryStack.CODEC.fieldOf("storageContent").forGetter(network -> {
                SingleSlurryStorage slurryStorage = (SingleSlurryStorage) network.storage;
                return new SlurryStack(slurryStorage.getResource(), slurryStorage.getAmount());
            }),
            (storage, storageContent) -> {
                SingleSlurryStorage slurryStorage = (SingleSlurryStorage) storage;
                slurryStorage.variant = storageContent.variant();
                slurryStorage.amount = storageContent.amount();
            },
            SlurryPipeNetwork::new);

    public static final PacketCodec<RegistryByteBuf, SlurryPipeNetwork> PACKET_CODEC =
            PipeNetwork.createPacketCodecWithRegistryByteBuf(
                    SlurryStack.PACKET_CODEC,
                    network -> {
                        SingleSlurryStorage slurryStorage = (SingleSlurryStorage) network.storage;
                        return new SlurryStack(slurryStorage.getResource(), slurryStorage.getAmount());
                    },
                    (storage, storageContent) -> {
                        SingleSlurryStorage slurryStorage = (SingleSlurryStorage) storage;
                        slurryStorage.variant = storageContent.variant();
                        slurryStorage.amount = storageContent.amount();
                    },
                    SlurryPipeNetwork::new);

    public static final Codec<Set<SlurryPipeNetwork>> SET_CODEC = ExtraCodecs.setOf(CODEC);
    public static final PacketCodec<RegistryByteBuf, Set<SlurryPipeNetwork>> SET_PACKET_CODEC =
            ExtraPacketCodecs.setOf(PACKET_CODEC);

    public SlurryPipeNetwork(UUID id) {
        super(id, TransferType.SLURRY);
    }

    @Override
    protected Storage<SlurryVariant> createStorage() {
        return new SingleSlurryStorage() {
            @Override
            protected long getCapacity(SlurryVariant variant) {
                return SlurryPipeNetwork.this.pipes.size() * FluidConstants.BUCKET;
            }
        };
    }

    @Override
    protected PipeNetworkType<Storage<SlurryVariant>, ? extends PipeNetwork<Storage<SlurryVariant>>> getType() {
        return PipeNetworkTypeInit.SLURRY;
    }

    public SlurryVariant getSlurryVariant() {
        return ((SingleSlurryStorage) this.storage).variant;
    }

    public long getSlurryAmount() {
        return ((SingleSlurryStorage) this.storage).amount;
    }
}
