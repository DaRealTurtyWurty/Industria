package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record SetMultiblockPieceCharPayload(BlockPos piecePos, char paletteChar) implements CustomPayload {
    public static final Id<SetMultiblockPieceCharPayload> ID = new Id<>(Industria.id("set_multiblock_piece_char"));
    public static final PacketCodec<RegistryByteBuf, SetMultiblockPieceCharPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, SetMultiblockPieceCharPayload::piecePos,
            PacketCodecs.INTEGER, payload -> (int) payload.paletteChar(),
            (piecePos, charCode) -> new SetMultiblockPieceCharPayload(piecePos, (char) (int) charCode)
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
