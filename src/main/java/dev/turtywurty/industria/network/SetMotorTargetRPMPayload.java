package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SetMotorTargetRPMPayload(float targetRPM) implements CustomPayload {
    public static final Id<SetMotorTargetRPMPayload> ID = new Id<>(Industria.id("set_motor_target_rpm"));
    public static final PacketCodec<ByteBuf, SetMotorTargetRPMPayload> CODEC =
            PacketCodec.tuple(PacketCodecs.FLOAT, SetMotorTargetRPMPayload::targetRPM, SetMotorTargetRPMPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
