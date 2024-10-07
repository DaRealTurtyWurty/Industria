package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record ChangeDrillOverflowModePayload(DrillBlockEntity.OverflowMethod overflowMethod) implements CustomPayload {
    public static final Id<ChangeDrillOverflowModePayload> ID = new Id<>(Industria.id("change_drill_overflow_mode"));

    public static final PacketCodec<ByteBuf, ChangeDrillOverflowModePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING.xmap(DrillBlockEntity.OverflowMethod::valueOf, DrillBlockEntity.OverflowMethod::name),
            ChangeDrillOverflowModePayload::overflowMethod,
            ChangeDrillOverflowModePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
