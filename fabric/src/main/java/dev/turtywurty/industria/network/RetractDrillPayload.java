package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public class RetractDrillPayload implements CustomPacketPayload {
    public static final Type<RetractDrillPayload> ID = new Type<>(Industria.id("retract_drill"));
    public static final StreamCodec<ByteBuf, RetractDrillPayload> CODEC =
            StreamCodec.ofMember((value, buf) -> {}, buf -> new RetractDrillPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
