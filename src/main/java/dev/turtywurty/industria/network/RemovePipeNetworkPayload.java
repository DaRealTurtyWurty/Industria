package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.TransferType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Uuids;
import net.minecraft.world.World;

import java.util.UUID;

public record RemovePipeNetworkPayload(RegistryKey<World> world, TransferType<?, ?, ?> transferType, UUID networkId) implements CustomPayload {
    public static final Id<RemovePipeNetworkPayload> ID = new Id<>(Industria.id("remove_pipe_network"));
    public static final PacketCodec<RegistryByteBuf, RemovePipeNetworkPayload> CODEC =
            PacketCodec.tuple(
                    RegistryKey.createPacketCodec(RegistryKeys.WORLD), RemovePipeNetworkPayload::world,
                    TransferType.PACKET_CODEC, RemovePipeNetworkPayload::transferType,
                    Uuids.PACKET_CODEC, RemovePipeNetworkPayload::networkId,
                    RemovePipeNetworkPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
