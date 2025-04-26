package dev.turtywurty.industria.pipe;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.multiblock.TransferType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.function.Supplier;

public record PipeNetworkManagerType<S, N extends PipeNetwork<S>>(TransferType<S, ?, ?> transferType,
                                                                  Supplier<PipeNetworkManager<S, N>> factory,
                                                                  MapCodec<? extends PipeNetworkManager<S, N>> codec,
                                                                  PacketCodec<RegistryByteBuf, ? extends PipeNetworkManager<S, N>> packetCodec) {
}
