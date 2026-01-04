package dev.turtywurty.industria.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.turtywurty.industria.persistent.WorldFluidPocketsState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;

public record FluidPocketsComponent(List<WorldFluidPocketsState.FluidPocket> pockets) {
    public static final Codec<FluidPocketsComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(WorldFluidPocketsState.FluidPocket.CODEC).fieldOf("pockets").forGetter(FluidPocketsComponent::pockets)
    ).apply(instance, FluidPocketsComponent::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidPocketsComponent> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.collection(ArrayList::new, WorldFluidPocketsState.FluidPocket.STREAM_CODEC),
                    FluidPocketsComponent::pockets,
                    FluidPocketsComponent::new);
}
