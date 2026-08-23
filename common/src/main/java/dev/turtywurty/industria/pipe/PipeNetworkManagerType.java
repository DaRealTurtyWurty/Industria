package dev.turtywurty.industria.pipe;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import dev.turtywurty.turtymultiloader.transfer.storage.ResourceStorage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

public record PipeNetworkManagerType<V extends ResourceVariant<?>, N extends PipeNetwork<V>>(
        TransferType<ResourceStorage<V>, V, Long> transferType,
        Supplier<PipeNetworkManager<V, N>> factory,
        MapCodec<? extends PipeNetworkManager<V, N>> codec,
        StreamCodec<RegistryFriendlyByteBuf, ? extends PipeNetworkManager<V, N>> packetCodec) {
}
