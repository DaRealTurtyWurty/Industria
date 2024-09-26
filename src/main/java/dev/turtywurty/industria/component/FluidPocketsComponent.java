package dev.turtywurty.industria.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.ArrayList;
import java.util.List;

public record FluidPocketsComponent(List<WorldFluidPocketsState.FluidPocket> pockets) {
    public static final Codec<FluidPocketsComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(WorldFluidPocketsState.FluidPocket.CODEC).fieldOf("pockets").forGetter(FluidPocketsComponent::pockets)
    ).apply(instance, FluidPocketsComponent::new));

    public static final PacketCodec<RegistryByteBuf, FluidPocketsComponent> PACKET_CODEC =
            PacketCodec.tuple(PacketCodecs.collection(ArrayList::new, WorldFluidPocketsState.FluidPocket.PACKET_CODEC),
                    FluidPocketsComponent::pockets,
                    FluidPocketsComponent::new);
}
