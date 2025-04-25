package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetwork;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public record AddPipeNetworkPayload(RegistryKey<World> world, TransferType<?, ?, ?> transferType,
                                    PipeNetwork<?> network) implements CustomPayload {
    public static final Id<AddPipeNetworkPayload> ID = new Id<>(Industria.id("add_pipe_network"));
    public static final PacketCodec<RegistryByteBuf, AddPipeNetworkPayload> CODEC =
            PacketCodec.tuple(
                    RegistryKey.createPacketCodec(RegistryKeys.WORLD), AddPipeNetworkPayload::world,
                    TransferType.PACKET_CODEC, AddPipeNetworkPayload::transferType,
                    PipeNetwork.PACKET_CODEC, AddPipeNetworkPayload::network,
                    AddPipeNetworkPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
