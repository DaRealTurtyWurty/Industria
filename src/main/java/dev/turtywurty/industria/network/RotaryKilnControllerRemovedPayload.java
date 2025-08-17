package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record RotaryKilnControllerRemovedPayload(BlockPos pos) implements CustomPayload {
    public static final Id<RotaryKilnControllerRemovedPayload> ID = new Id<>(Industria.id("rotary_kiln_controller_removed"));

    public static final PacketCodec<ByteBuf, RotaryKilnControllerRemovedPayload> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC,
                    RotaryKilnControllerRemovedPayload::pos,
                    RotaryKilnControllerRemovedPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
