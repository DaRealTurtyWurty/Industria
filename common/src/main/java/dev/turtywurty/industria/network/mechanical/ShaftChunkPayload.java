package dev.turtywurty.industria.network.mechanical;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.mechanical.ShaftRotation;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.List;

public record ShaftChunkPayload(ResourceKey<Level> level, ChunkPos chunk, List<NetworkShafts> networks)
    implements CustomPacketPayload {
    public static final Type<ShaftChunkPayload> ID = new Type<>(Industria.id("shaft_chunk"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShaftChunkPayload> CODEC = StreamCodec.composite(
        ResourceKey.streamCodec(Registries.DIMENSION), ShaftChunkPayload::level,
        ChunkPos.STREAM_CODEC, ShaftChunkPayload::chunk,
        NetworkShafts.STREAM_CODEC.apply(ByteBufCodecs.list()), ShaftChunkPayload::networks,
        ShaftChunkPayload::new);

    @Override
    public Type<ShaftChunkPayload> type() {
        return ID;
    }

    public record NetworkShafts(long networkId, ShaftRotation rotation, List<BlockPos> positions) {
        private static final StreamCodec<ByteBuf, NetworkShafts> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, NetworkShafts::networkId,
            ShaftRotation.STREAM_CODEC, NetworkShafts::rotation,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), NetworkShafts::positions,
            NetworkShafts::new);
    }
}
