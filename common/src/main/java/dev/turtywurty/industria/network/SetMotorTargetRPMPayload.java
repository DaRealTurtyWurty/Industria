package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetMotorTargetRPMPayload(float targetRPM) implements CustomPacketPayload {
    public static final Type<SetMotorTargetRPMPayload> ID = new Type<>(Industria.id("set_motor_target_rpm"));
    public static final StreamCodec<ByteBuf, SetMotorTargetRPMPayload> CODEC =
            StreamCodec.composite(ByteBufCodecs.FLOAT, SetMotorTargetRPMPayload::targetRPM, SetMotorTargetRPMPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
