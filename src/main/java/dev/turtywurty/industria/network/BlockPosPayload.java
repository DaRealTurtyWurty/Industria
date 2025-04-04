package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record BlockPosPayload(BlockPos pos) implements HasPositionPayload {
    public static final Id<BlockPosPayload> ID = new Id<>(Industria.id("block_pos"));
    public static final PacketCodec<RegistryByteBuf, BlockPosPayload> CODEC =
            PacketCodec.tuple(BlockPos.PACKET_CODEC, BlockPosPayload::pos, BlockPosPayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
