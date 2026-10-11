package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CompleteMoldPayload(int containerId, Identifier moldId) implements CustomPacketPayload {
    public static final Type<CompleteMoldPayload> ID = new Type<>(Industria.id("complete_mold"));
    public static final StreamCodec<ByteBuf, CompleteMoldPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.VAR_INT, CompleteMoldPayload::containerId,
                    Identifier.STREAM_CODEC, CompleteMoldPayload::moldId,
                    CompleteMoldPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
