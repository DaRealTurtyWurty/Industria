package dev.turtywurty.industria.pipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record PipeNetworkType<S, N extends PipeNetwork<S>>(
        MapCodec<? extends N> codec,
        StreamCodec<RegistryFriendlyByteBuf, ? extends N> packetCodec) {
}
