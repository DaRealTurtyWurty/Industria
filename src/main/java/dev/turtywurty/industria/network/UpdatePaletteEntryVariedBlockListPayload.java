package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.VariedBlockList;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record UpdatePaletteEntryVariedBlockListPayload(char paletteChar,
                                                       VariedBlockList variedBlockList) implements CustomPayload {
    public static final CustomPayload.Id<UpdatePaletteEntryVariedBlockListPayload> ID =
            new CustomPayload.Id<>(Industria.id("update_palette_entry_varied_block_list"));
    public static final PacketCodec<RegistryByteBuf, UpdatePaletteEntryVariedBlockListPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, payload -> (int) payload.paletteChar(),
            VariedBlockList.PACKET_CODEC, UpdatePaletteEntryVariedBlockListPayload::variedBlockList,
            (character, list) -> new UpdatePaletteEntryVariedBlockListPayload((char) (int) character, list)
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
