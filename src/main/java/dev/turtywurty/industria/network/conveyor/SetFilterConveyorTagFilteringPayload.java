package dev.turtywurty.industria.network.conveyor;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetFilterConveyorTagFilteringPayload(boolean tagFiltering) implements CustomPacketPayload {
    public static final Type<SetFilterConveyorTagFilteringPayload> ID =
            new Type<>(Industria.id("set_filter_conveyor_tag_filtering"));

    public static final StreamCodec<ByteBuf, SetFilterConveyorTagFilteringPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, SetFilterConveyorTagFilteringPayload::tagFiltering,
                    SetFilterConveyorTagFilteringPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
