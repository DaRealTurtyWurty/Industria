package dev.turtywurty.industria.pipe.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.util.Uuids;

import java.util.UUID;

public class FluidPipeNetwork extends PipeNetwork<Storage<FluidVariant>> {
    public static final MapCodec<FluidPipeNetwork> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Uuids.CODEC.fieldOf("id").forGetter(FluidPipeNetwork::getId),
                    BLOCK_POS_SET_CODEC.fieldOf("pipes").forGetter(FluidPipeNetwork::getPipes),
                    BLOCK_POS_SET_CODEC.fieldOf("connectedBlocks").forGetter(FluidPipeNetwork::getConnectedBlocks),
                    TransferType.CODEC.fieldOf("transferType").forGetter(FluidPipeNetwork::getTransferType),
                    Codec.LONG.fieldOf("storageAmount").forGetter(FluidPipeNetwork::getFluidAmount),
                    FluidVariant.CODEC.fieldOf("fluidVariant").forGetter(FluidPipeNetwork::getFluidVariant)
            ).apply(instance, (id, pipes, connectedBlocks, transferType, storageAmount, fluidVariant) -> {
                var network = new FluidPipeNetwork(id);
                network.pipes.addAll(pipes);
                network.connectedBlocks.addAll(connectedBlocks);
                ((SingleFluidStorage) network.storage).amount = storageAmount;
                ((SingleFluidStorage) network.storage).variant = fluidVariant;

                return network;
            }));

    public FluidPipeNetwork(UUID id) {
        super(id, TransferType.FLUID);
    }

    @Override
    protected Storage<FluidVariant> createStorage() {
        return new SingleFluidStorage() {
            @Override
            protected long getCapacity(FluidVariant variant) {
                return FluidPipeNetwork.this.pipes.size() * FluidConstants.BUCKET;
            }
        };
    }

    @Override
    public MapCodec<? extends PipeNetwork<?>> getCodec() {
        return CODEC;
    }

    public long getFluidAmount() {
        return ((SingleFluidStorage) this.storage).getAmount();
    }

    public FluidVariant getFluidVariant() {
        return ((SingleFluidStorage) this.storage).getResource();
    }
}
