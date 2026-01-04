package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RotaryKilnControllerRemovedPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<RotaryKilnControllerRemovedPayload> ID = new Type<>(Industria.id("rotary_kiln_controller_removed"));

    public static final StreamCodec<ByteBuf, RotaryKilnControllerRemovedPayload> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC,
                    RotaryKilnControllerRemovedPayload::pos,
                    RotaryKilnControllerRemovedPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
