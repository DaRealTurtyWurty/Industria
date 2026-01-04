package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import dev.turtywurty.industria.multiblock.VariedBlockList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdatePaletteEntryVariedBlockListPayload(char paletteChar,
                                                       VariedBlockList variedBlockList) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdatePaletteEntryVariedBlockListPayload> ID =
            new CustomPacketPayload.Type<>(Industria.id("update_palette_entry_varied_block_list"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdatePaletteEntryVariedBlockListPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, payload -> (int) payload.paletteChar(),
            VariedBlockList.STREAM_CODEC, UpdatePaletteEntryVariedBlockListPayload::variedBlockList,
            (character, list) -> new UpdatePaletteEntryVariedBlockListPayload((char) (int) character, list)
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
