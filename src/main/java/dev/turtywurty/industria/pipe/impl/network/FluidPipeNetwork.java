package dev.turtywurty.industria.pipe.impl.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.blockentity.util.fluid.FluidStack;
import dev.turtywurty.industria.init.PipeNetworkTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkType;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ExtraPacketCodecs;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.base.SingleFluidStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Set;
import java.util.UUID;

public class FluidPipeNetwork extends PipeNetwork<Storage<FluidVariant>> {
    public static final MapCodec<FluidPipeNetwork> CODEC = PipeNetwork.createCodec(
            FluidStack.CODEC.fieldOf("storageContent").forGetter(network -> {
                SingleFluidStorage fluidStorage = (SingleFluidStorage) network.storage;
                return new FluidStack(fluidStorage.getResource(), fluidStorage.getAmount());
            }),
            (storage, storageContent) -> {
                SingleFluidStorage fluidStorage = (SingleFluidStorage) storage;
                fluidStorage.variant = storageContent.variant();
                fluidStorage.amount = storageContent.amount();
            },
            FluidPipeNetwork::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidPipeNetwork> STREAM_CODEC =
            PipeNetwork.createPacketCodec(
                    FluidStack.STREAM_CODEC,
                    network -> {
                        SingleFluidStorage fluidStorage = (SingleFluidStorage) network.storage;
                        return new FluidStack(fluidStorage.getResource(), fluidStorage.getAmount());
                    },
                    (storage, storageContent) -> {
                        SingleFluidStorage fluidStorage = (SingleFluidStorage) storage;
                        fluidStorage.variant = storageContent.variant();
                        fluidStorage.amount = storageContent.amount();
                    },
                    FluidPipeNetwork::new);

    public static final Codec<Set<FluidPipeNetwork>> SET_CODEC = ExtraCodecs.setOf(CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Set<FluidPipeNetwork>> SET_STREAM_CODEC =
            ExtraPacketCodecs.setOf(STREAM_CODEC);

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
    protected PipeNetworkType<Storage<FluidVariant>, FluidPipeNetwork> getType() {
        return PipeNetworkTypeInit.FLUID;
    }

    public long getFluidAmount() {
        return ((SingleFluidStorage) this.storage).getAmount();
    }

    public FluidVariant getFluidVariant() {
        return ((SingleFluidStorage) this.storage).getResource();
    }
}
