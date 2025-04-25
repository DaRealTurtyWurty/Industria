package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;

public record SyncPipeNetworkManagerPayload(TransferType<?, ?, ?> transferType, RegistryKey<World> dimension,
                                            Map<BlockPos, UUID> pipeToNetworkId) implements CustomPayload {
    public static final Id<SyncPipeNetworkManagerPayload> ID = new Id<>(Industria.id("sync_pipe_network_manager"));
    public static final PacketCodec<RegistryByteBuf, SyncPipeNetworkManagerPayload> CODEC =
            PacketCodec.tuple(
                    TransferType.PACKET_CODEC, SyncPipeNetworkManagerPayload::transferType,
                    RegistryKey.createPacketCodec(RegistryKeys.WORLD), SyncPipeNetworkManagerPayload::dimension,
                    PipeNetworkManager.PIPE_TO_NETWORK_ID_PACKET_CODEC, SyncPipeNetworkManagerPayload::pipeToNetworkId,
                    SyncPipeNetworkManagerPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
