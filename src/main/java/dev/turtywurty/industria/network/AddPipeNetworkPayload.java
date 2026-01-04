package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record AddPipeNetworkPayload(ResourceKey<Level> world, TransferType<?, ?, ?> transferType,
                                    PipeNetwork<?> network) implements CustomPacketPayload {
    public static final Type<AddPipeNetworkPayload> ID = new Type<>(Industria.id("add_pipe_network"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddPipeNetworkPayload> CODEC =
            StreamCodec.composite(
                    ResourceKey.streamCodec(Registries.DIMENSION), AddPipeNetworkPayload::world,
                    TransferType.STREAM_CODEC, AddPipeNetworkPayload::transferType,
                    PipeNetwork.STREAM_CODEC, AddPipeNetworkPayload::network,
                    AddPipeNetworkPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
