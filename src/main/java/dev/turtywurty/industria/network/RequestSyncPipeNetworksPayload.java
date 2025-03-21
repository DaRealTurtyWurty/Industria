package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public class RequestSyncPipeNetworksPayload implements CustomPayload {
    public static final Id<RequestSyncPipeNetworksPayload> ID = new Id<>(Industria.id("request_sync_pipe_networks"));
    public static final PacketCodec<ByteBuf, RequestSyncPipeNetworksPayload> CODEC =
            PacketCodec.of((value, buf) -> {}, buf -> new RequestSyncPipeNetworksPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
