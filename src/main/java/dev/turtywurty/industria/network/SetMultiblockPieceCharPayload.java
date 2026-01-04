package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SetMultiblockPieceCharPayload(BlockPos piecePos, char paletteChar) implements CustomPacketPayload {
    public static final Type<SetMultiblockPieceCharPayload> ID = new Type<>(Industria.id("set_multiblock_piece_char"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetMultiblockPieceCharPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetMultiblockPieceCharPayload::piecePos,
            ByteBufCodecs.INT, payload -> (int) payload.paletteChar(),
            (piecePos, charCode) -> new SetMultiblockPieceCharPayload(piecePos, (char) (int) charCode)
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
