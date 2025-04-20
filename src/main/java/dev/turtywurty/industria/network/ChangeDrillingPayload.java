package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ChangeDrillingPayload(boolean drilling) implements CustomPayload {
    public static final Id<ChangeDrillingPayload> ID = new Id<>(Industria.id("change_drilling"));
    public static final PacketCodec<ByteBuf, ChangeDrillingPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.BOOLEAN, ChangeDrillingPayload::drilling, ChangeDrillingPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
