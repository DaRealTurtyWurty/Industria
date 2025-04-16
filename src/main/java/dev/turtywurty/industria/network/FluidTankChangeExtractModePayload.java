package dev.turtywurty.industria.network;

import dev.turtywurty.industria.Industria;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record FluidTankChangeExtractModePayload(boolean extractMode) implements CustomPayload {
    public static final Id<FluidTankChangeExtractModePayload> ID = new Id<>(Industria.id("fluid_tank_change_extract_mode"));
    public static final PacketCodec<RegistryByteBuf, FluidTankChangeExtractModePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, FluidTankChangeExtractModePayload::extractMode, FluidTankChangeExtractModePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
