package dev.turtywurty.industria.network.conveyor;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetFilterConveyorBlacklistModePayload(boolean blacklistMode) implements CustomPacketPayload {
    public static final Type<SetFilterConveyorBlacklistModePayload> ID =
            new Type<>(Industria.id("set_filter_conveyor_blacklist_mode"));

    public static final StreamCodec<ByteBuf, SetFilterConveyorBlacklistModePayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SetFilterConveyorBlacklistModePayload::blacklistMode,
                    SetFilterConveyorBlacklistModePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
