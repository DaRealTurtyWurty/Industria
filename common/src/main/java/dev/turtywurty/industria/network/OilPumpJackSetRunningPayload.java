package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OilPumpJackSetRunningPayload(boolean isRunning) implements CustomPacketPayload {
    public static final Type<OilPumpJackSetRunningPayload> ID = new Type<>(Industria.id("oil_pump_jack_set_running_payload"));

    public static final StreamCodec<ByteBuf, OilPumpJackSetRunningPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.BOOL, OilPumpJackSetRunningPayload::isRunning,
                    OilPumpJackSetRunningPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
