package dev.turtywurty.industria.mechanical;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ShaftRotation(double angleRadians, double rpm, long gameTick) {
    public static final StreamCodec<ByteBuf, ShaftRotation> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, ShaftRotation::angleRadians,
        ByteBufCodecs.DOUBLE, ShaftRotation::rpm,
        ByteBufCodecs.VAR_LONG, ShaftRotation::gameTick,
        ShaftRotation::new);

    public float angleAt(long clientGameTick, float partialTick) {
        double elapsedTicks = Math.max(0.0, clientGameTick - gameTick + partialTick);
        double radiansPerTick = rpm * MechanicalSimulation.RADIANS_PER_RPM * MechanicalSimulation.SECONDS_PER_TICK;
        return (float) Math.IEEEremainder(angleRadians + elapsedTicks * radiansPerTick, Math.TAU);
    }
}
