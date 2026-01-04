package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.TransferType;
import dev.turtywurty.industria.pipe.PipeNetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;

public record SyncPipeNetworkManagerPayload(TransferType<?, ?, ?> transferType, ResourceKey<Level> dimension,
                                            Map<BlockPos, UUID> pipeToNetworkId) implements CustomPacketPayload {
    public static final Type<SyncPipeNetworkManagerPayload> ID = new Type<>(Industria.id("sync_pipe_network_manager"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncPipeNetworkManagerPayload> CODEC =
            StreamCodec.composite(
                    TransferType.STREAM_CODEC, SyncPipeNetworkManagerPayload::transferType,
                    ResourceKey.streamCodec(Registries.DIMENSION), SyncPipeNetworkManagerPayload::dimension,
                    PipeNetworkManager.PIPE_TO_NETWORK_ID_STREAM_CODEC, SyncPipeNetworkManagerPayload::pipeToNetworkId,
                    SyncPipeNetworkManagerPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
