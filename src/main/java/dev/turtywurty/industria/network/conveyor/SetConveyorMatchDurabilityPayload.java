package dev.turtywurty.industria.network.conveyor;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetConveyorMatchDurabilityPayload(boolean matchDurability) implements CustomPacketPayload {
    public static final Type<SetConveyorMatchDurabilityPayload> ID =
            new Type<>(Industria.id("set_conveyor_match_durability"));

    public static final StreamCodec<ByteBuf, SetConveyorMatchDurabilityPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SetConveyorMatchDurabilityPayload::matchDurability,
                    SetConveyorMatchDurabilityPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
