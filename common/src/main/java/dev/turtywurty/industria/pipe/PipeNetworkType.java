package dev.turtywurty.industria.pipe;

import com.mojang.serialization.MapCodec;
import dev.turtywurty.turtymultiloader.transfer.resource.ResourceVariant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record PipeNetworkType<V extends ResourceVariant<?>, N extends PipeNetwork<V>>(
        MapCodec<? extends N> codec,
        StreamCodec<RegistryFriendlyByteBuf, ? extends N> packetCodec) {
}
