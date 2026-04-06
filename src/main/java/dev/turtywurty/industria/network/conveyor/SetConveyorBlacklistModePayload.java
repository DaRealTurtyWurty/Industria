package dev.turtywurty.industria.network.conveyor;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetConveyorBlacklistModePayload(boolean blacklistMode) implements CustomPacketPayload {
    public static final Type<SetConveyorBlacklistModePayload> ID =
            new Type<>(Industria.id("set_conveyor_blacklist_mode"));

    public static final StreamCodec<ByteBuf, SetConveyorBlacklistModePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SetConveyorBlacklistModePayload::blacklistMode,
                    SetConveyorBlacklistModePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
