package dev.turtywurty.industria.pipe;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.industria.multiblock.TransferType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.function.Function;

public record PipeNetworkManagerType<S, N extends PipeNetwork<S>>(TransferType<S, ?, ?> transferType,
                                                                  Function<RegistryKey<World>, PipeNetworkManager<S, N>> factory,
                                                                  MapCodec<? extends PipeNetworkManager<S, N>> codec,
                                                                  PacketCodec<RegistryByteBuf, ? extends PipeNetworkManager<S, N>> packetCodec) {
}
