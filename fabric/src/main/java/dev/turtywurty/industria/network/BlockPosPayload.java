package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record BlockPosPayload(BlockPos pos) implements HasPositionPayload {
    public static final Type<BlockPosPayload> ID = new Type<>(Industria.id("block_pos"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockPosPayload> CODEC =
            StreamCodec.composite(BlockPos.STREAM_CODEC, BlockPosPayload::pos, BlockPosPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
