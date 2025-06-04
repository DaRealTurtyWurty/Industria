package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record OilPumpJackSetRunningPayload(boolean isRunning) implements CustomPayload {
    public static final Id<OilPumpJackSetRunningPayload> ID = new Id<>(Industria.id("oil_pump_jack_set_running_payload"));

    public static final PacketCodec<ByteBuf, OilPumpJackSetRunningPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.BOOLEAN, OilPumpJackSetRunningPayload::isRunning,
                    OilPumpJackSetRunningPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
