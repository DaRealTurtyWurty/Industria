package dev.turtywurty.industria.network.conveyor;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetConveyorTagFilteringPayload(boolean tagFiltering) implements CustomPacketPayload {
    public static final Type<SetConveyorTagFilteringPayload> ID =
            new Type<>(Industria.id("set_conveyor_tag_filtering"));

    public static final StreamCodec<ByteBuf, SetConveyorTagFilteringPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SetConveyorTagFilteringPayload::tagFiltering,
                    SetConveyorTagFilteringPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
