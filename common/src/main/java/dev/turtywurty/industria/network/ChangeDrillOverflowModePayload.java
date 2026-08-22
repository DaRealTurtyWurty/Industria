package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.blockentity.DrillBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ChangeDrillOverflowModePayload(DrillBlockEntity.OverflowMethod overflowMethod) implements CustomPacketPayload {
    public static final Type<ChangeDrillOverflowModePayload> ID = new Type<>(Industria.id("change_drill_overflow_mode"));

    public static final StreamCodec<ByteBuf, ChangeDrillOverflowModePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.map(DrillBlockEntity.OverflowMethod::valueOf, DrillBlockEntity.OverflowMethod::name),
            ChangeDrillOverflowModePayload::overflowMethod,
            ChangeDrillOverflowModePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
