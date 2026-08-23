package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ChangeDrillingPayload(boolean drilling) implements CustomPacketPayload {
    public static final Type<ChangeDrillingPayload> ID = new Type<>(Industria.id("change_drilling"));
    public static final StreamCodec<ByteBuf, ChangeDrillingPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, ChangeDrillingPayload::drilling, ChangeDrillingPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
