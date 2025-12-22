package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record UpdatePaletteEntryNamePayload(char paletteChar, String name) implements CustomPayload {
    public static final Id<UpdatePaletteEntryNamePayload> ID = new Id<>(Industria.id("update_palette_entry_name"));
    public static final PacketCodec<RegistryByteBuf, UpdatePaletteEntryNamePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, payload -> (int) payload.paletteChar(),
            PacketCodecs.STRING, UpdatePaletteEntryNamePayload::name,
            (character, name) -> new UpdatePaletteEntryNamePayload((char) (int) character, name)
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
