package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record DeletePaletteEntryPayload(char paletteChar) implements CustomPayload {
    public static final Id<DeletePaletteEntryPayload> ID = new Id<>(Industria.id("delete_palette_entry"));
    public static final PacketCodec<RegistryByteBuf, DeletePaletteEntryPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, payload -> (int) payload.paletteChar(),
            character -> new DeletePaletteEntryPayload((char) (int) character)
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
