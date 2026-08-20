package dev.turtywurty.industria.pipe.impl.network;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.turtywurty.gasapi.api.GasVariant;
import dev.turtywurty.gasapi.api.storage.SingleGasStorage;
import dev.turtywurty.industria.blockentity.util.gas.GasStack;
import dev.turtywurty.industria.init.PipeNetworkTypeInit;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import dev.turtywurty.industria.pipe.PipeNetworkType;
import dev.turtywurty.industria.util.ExtraCodecs;
import dev.turtywurty.industria.util.ExtraStreamCodecs;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Set;
import java.util.UUID;

public class GasPipeNetwork extends PipeNetwork<Storage<GasVariant>> {
    public static final MapCodec<GasPipeNetwork> CODEC = PipeNetwork.createCodec(
            GasStack.CODEC.fieldOf("storageContent").forGetter(network -> {
                SingleGasStorage gasStorage = (SingleGasStorage) network.storage;
                return new GasStack(gasStorage.getResource(), gasStorage.getAmount());
            }),
            (storage, storageContent) -> {
                SingleGasStorage gasStorage = (SingleGasStorage) storage;
                gasStorage.variant = storageContent.variant();
                gasStorage.amount = storageContent.amount();
            },
            GasPipeNetwork::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, GasPipeNetwork> STREAM_CODEC =
            PipeNetwork.createPacketCodec(
                    GasStack.STREAM_CODEC,
                    network -> {
                        SingleGasStorage gasStorage = (SingleGasStorage) network.storage;
                        return new GasStack(gasStorage.getResource(), gasStorage.getAmount());
                    },
                    (storage, storageContent) -> {
                        SingleGasStorage gasStorage = (SingleGasStorage) storage;
                        gasStorage.variant = storageContent.variant();
                        gasStorage.amount = storageContent.amount();
                    },
                    GasPipeNetwork::new);

    public static final Codec<Set<GasPipeNetwork>> SET_CODEC = ExtraCodecs.setOf(CODEC);
    public static final StreamCodec<RegistryFriendlyByteBuf, Set<GasPipeNetwork>> SET_STREAM_CODEC =
            ExtraStreamCodecs.setOf(STREAM_CODEC);

    public GasPipeNetwork(UUID id) {
        super(id, TransferType.GAS);
    }

    @Override
    protected Storage<GasVariant> createStorage() {
        return new SingleGasStorage() {
            @Override
            protected long getCapacity(GasVariant variant) {
                return GasPipeNetwork.this.pipes.size() * FluidConstants.BUCKET;
            }
        };
    }

    @Override
    protected PipeNetworkType<Storage<GasVariant>, GasPipeNetwork> getType() {
        return PipeNetworkTypeInit.GAS;
    }
}
