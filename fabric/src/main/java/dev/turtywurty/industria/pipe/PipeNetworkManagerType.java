package dev.turtywurty.industria.pipe;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.multiblock.TransferType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Supplier;

public record PipeNetworkManagerType<S, N extends PipeNetwork<S>>(TransferType<S, ?, ?> transferType,
                                                                  Supplier<PipeNetworkManager<S, N>> factory,
                                                                  MapCodec<? extends PipeNetworkManager<S, N>> codec,
                                                                  StreamCodec<RegistryFriendlyByteBuf, ? extends PipeNetworkManager<S, N>> packetCodec) {
}
