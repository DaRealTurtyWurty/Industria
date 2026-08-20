package dev.turtywurty.industria.network.conveyor;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetConveyorMatchComponentsPayload(boolean matchComponents) implements CustomPacketPayload {
    public static final Type<SetConveyorMatchComponentsPayload> ID =
            new Type<>(Industria.id("set_conveyor_match_components"));

    public static final StreamCodec<ByteBuf, SetConveyorMatchComponentsPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SetConveyorMatchComponentsPayload::matchComponents,
                    SetConveyorMatchComponentsPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
