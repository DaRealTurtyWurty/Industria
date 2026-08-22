package dev.turtywurty.industria.network.conveyor;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public record SetConveyorFilterTagPayload(TagKey<Item> filterTag) implements CustomPacketPayload {
    public static final Type<SetConveyorFilterTagPayload> ID =
            new Type<>(Industria.id("set_conveyor_filter_tag"));

    public static final StreamCodec<ByteBuf, SetConveyorFilterTagPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeBoolean(payload.filterTag != null);
                if (payload.filterTag != null) {
                    TagKey.streamCodec(Registries.ITEM).encode(buf, payload.filterTag);
                }
            },
            buf -> new SetConveyorFilterTagPayload(
                    buf.readBoolean()
                            ? TagKey.streamCodec(Registries.ITEM).decode(buf)
                            : null));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
