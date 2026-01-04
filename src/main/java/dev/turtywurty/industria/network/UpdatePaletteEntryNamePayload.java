package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record UpdatePaletteEntryNamePayload(char paletteChar, String name) implements CustomPacketPayload {
    public static final Type<UpdatePaletteEntryNamePayload> ID = new Type<>(Industria.id("update_palette_entry_name"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdatePaletteEntryNamePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, payload -> (int) payload.paletteChar(),
            ByteBufCodecs.STRING_UTF8, UpdatePaletteEntryNamePayload::name,
            (character, name) -> new UpdatePaletteEntryNamePayload((char) (int) character, name)
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
