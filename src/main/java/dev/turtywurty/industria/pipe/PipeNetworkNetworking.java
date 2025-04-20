package dev.turtywurty.industria.pipe;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.persistent.WorldPipeNetworks;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.UUID;

public class PipeNetworkNetworking {
    public static void sync(ServerPlayerEntity player) {
        ServerWorld serverWorld = player.getServerWorld();
        RegistryByteBuf buf = new RegistryByteBuf(PacketByteBufs.create(), serverWorld.getRegistryManager());
        WorldPipeNetworks.PACKET_CODEC.encode(buf, WorldPipeNetworks.getOrCreate(serverWorld));

        UUID packetGroupId = UUID.randomUUID();
        final int maxPacketSize = 1048576 - 16; // A UUID is 16 bytes

        int readableBytes = buf.readableBytes();
        int sliceIndex = 0;
        while (sliceIndex < readableBytes) {
            int sliceLength = Math.min(readableBytes - sliceIndex, maxPacketSize);
            PacketByteBuf slicedBuf = PacketByteBufs.slice(buf, sliceIndex, sliceLength);
            ServerPlayNetworking.send(player, Payload.createPayload(packetGroupId, slicedBuf));
            sliceIndex += sliceLength;
        }

        ServerPlayNetworking.send(player, Payload.createPayload(packetGroupId, PacketByteBufs.empty()));
    }

    public record Payload(UUID uuid, byte[] data) implements CustomPayload {
        public static final Id<Payload> ID = new Id<>(Industria.id("pipe_network"));
        public static PacketCodec<PacketByteBuf, Payload> CODEC = CustomPayload.codecOf(Payload::write, Payload::new);

        public static Payload createPayload(UUID uuid, PacketByteBuf buf) {
            if (buf.readableBytes() < 1) {
                return new Payload(uuid, new byte[0]);
            }

            return new Payload(uuid, buf.array());
        }

        Payload(PacketByteBuf buf) {
            this(buf.readUuid(), readAllBytes(buf));
        }

        private void write(PacketByteBuf buf) {
            buf.writeUuid(uuid);
            buf.writeBytes(data);
        }

        private static byte[] readAllBytes(PacketByteBuf buf) {
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            return bytes;
        }

        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
}