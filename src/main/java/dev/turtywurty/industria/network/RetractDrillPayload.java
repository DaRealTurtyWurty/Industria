package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public class RetractDrillPayload implements CustomPayload {
    public static final Id<RetractDrillPayload> ID = new Id<>(Industria.id("retract_drill"));
    public static final PacketCodec<ByteBuf, RetractDrillPayload> CODEC =
            PacketCodec.of((value, buf) -> {
            }, buf -> new RetractDrillPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
