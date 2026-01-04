package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record OpenSeismicScannerPayload(ItemStack stack) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenSeismicScannerPayload> ID =
            new CustomPacketPayload.Type<>(Industria.id("open_seismic_scanner"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenSeismicScannerPayload> CODEC =
            StreamCodec.composite(ItemStack.STREAM_CODEC, OpenSeismicScannerPayload::stack, OpenSeismicScannerPayload::new);

    public OpenSeismicScannerPayload {
        if (stack == null) {
            throw new IllegalArgumentException("Stack cannot be null!");
        }
    }

    @Override
    public Type<OpenSeismicScannerPayload> type() {
        return ID;
    }
}
