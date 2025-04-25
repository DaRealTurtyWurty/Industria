package dev.turtywurty.industria.pipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

public record PipeNetworkType<S, N extends PipeNetwork<S>>(
        MapCodec<? extends N> codec,
        PacketCodec<RegistryByteBuf, ? extends N> packetCodec) {
}
