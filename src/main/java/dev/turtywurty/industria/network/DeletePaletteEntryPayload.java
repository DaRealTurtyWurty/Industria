package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record DeletePaletteEntryPayload(char paletteChar) implements CustomPacketPayload {
    public static final Type<DeletePaletteEntryPayload> ID = new Type<>(Industria.id("delete_palette_entry"));
    public static final StreamCodec<RegistryFriendlyByteBuf, DeletePaletteEntryPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, payload -> (int) payload.paletteChar(),
            character -> new DeletePaletteEntryPayload((char) (int) character)
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
