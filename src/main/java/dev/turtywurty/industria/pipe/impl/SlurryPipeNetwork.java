package dev.turtywurty.industria.pipe.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.fabricslurryapi.api.SlurryVariant;
import dev.turtywurty.fabricslurryapi.api.storage.SingleSlurryStorage;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.util.Uuids;

import java.util.UUID;

public class SlurryPipeNetwork extends PipeNetwork<Storage<SlurryVariant>> {
    public static final MapCodec<SlurryPipeNetwork> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Uuids.CODEC.fieldOf("id").forGetter(SlurryPipeNetwork::getId),
                    BLOCK_POS_SET_CODEC.fieldOf("pipes").forGetter(SlurryPipeNetwork::getPipes),
                    BLOCK_POS_SET_CODEC.fieldOf("connectedBlocks").forGetter(SlurryPipeNetwork::getConnectedBlocks),
                    TransferType.CODEC.fieldOf("transferType").forGetter(SlurryPipeNetwork::getTransferType),
                    Codec.LONG.fieldOf("storageAmount").forGetter(SlurryPipeNetwork::getSlurryAmount),
                    SlurryVariant.CODEC.fieldOf("fluidVariant").forGetter(SlurryPipeNetwork::getSlurryVariant)
            ).apply(instance, (id, pipes, connectedBlocks, transferType, storageAmount, fluidVariant) -> {
                var network = new SlurryPipeNetwork(id);
                network.pipes.addAll(pipes);
                network.connectedBlocks.addAll(connectedBlocks);
                ((SingleSlurryStorage) network.storage).amount = storageAmount;
                ((SingleSlurryStorage) network.storage).variant = fluidVariant;

                return network;
            }));

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
    public MapCodec<? extends PipeNetwork<?>> getCodec() {
        return CODEC;
    }

    public SlurryVariant getSlurryVariant() {
        return ((SingleSlurryStorage) this.storage).variant;
    }

    public long getSlurryAmount() {
        return ((SingleSlurryStorage) this.storage).amount;
    }
}
