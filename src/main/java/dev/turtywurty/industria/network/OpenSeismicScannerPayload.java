package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record OpenSeismicScannerPayload(ItemStack stack) implements CustomPayload {
    public static final CustomPayload.Id<OpenSeismicScannerPayload> ID =
            new CustomPayload.Id<>(Industria.id("open_seismic_scanner"));
    public static final PacketCodec<RegistryByteBuf, OpenSeismicScannerPayload> CODEC =
            PacketCodec.tuple(ItemStack.PACKET_CODEC, OpenSeismicScannerPayload::stack, OpenSeismicScannerPayload::new);

    public OpenSeismicScannerPayload {
        if (stack == null) {
            throw new IllegalArgumentException("Stack cannot be null!");
        }
    }

    @Override
    public Id<OpenSeismicScannerPayload> getId() {
        return ID;
    }
}
