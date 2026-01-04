package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.TransferType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

public record RemovePipeNetworkPayload(ResourceKey<Level> world, TransferType<?, ?, ?> transferType, UUID networkId) implements CustomPacketPayload {
    public static final Type<RemovePipeNetworkPayload> ID = new Type<>(Industria.id("remove_pipe_network"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemovePipeNetworkPayload> CODEC =
            StreamCodec.composite(
                    ResourceKey.streamCodec(Registries.DIMENSION), RemovePipeNetworkPayload::world,
                    TransferType.STREAM_CODEC, RemovePipeNetworkPayload::transferType,
                    UUIDUtil.STREAM_CODEC, RemovePipeNetworkPayload::networkId,
                    RemovePipeNetworkPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
