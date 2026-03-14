package dev.turtywurty.industria.network.conveyor;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetFilterConveyorMatchComponentsPayload(boolean matchComponents) implements CustomPacketPayload {
    public static final Type<SetFilterConveyorMatchComponentsPayload> ID =
            new Type<>(Industria.id("set_filter_conveyor_match_components"));

    public static final StreamCodec<ByteBuf, SetFilterConveyorMatchComponentsPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SetFilterConveyorMatchComponentsPayload::matchComponents,
                    SetFilterConveyorMatchComponentsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
