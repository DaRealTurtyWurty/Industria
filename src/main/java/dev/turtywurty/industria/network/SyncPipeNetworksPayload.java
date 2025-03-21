package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record SyncPipeNetworksPayload(String transferType, UUID packetGroupId, int index, int chunks, String data) implements CustomPayload {
    public static final Id<SyncPipeNetworksPayload> ID = new Id<>(Industria.id("sync_pipe_networks"));
    public static final PacketCodec<PacketByteBuf, SyncPipeNetworksPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING, SyncPipeNetworksPayload::transferType,
                    Uuids.PACKET_CODEC, SyncPipeNetworksPayload::packetGroupId,
                    PacketCodecs.INTEGER, SyncPipeNetworksPayload::index,
                    PacketCodecs.INTEGER, SyncPipeNetworksPayload::chunks,
                    PacketCodecs.STRING, SyncPipeNetworksPayload::data, SyncPipeNetworksPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
